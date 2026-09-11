"""Versioned MySQL migrations. Defaults to plan/preflight; --apply writes explicitly.

Credentials: BIZ_DB_HOST, BIZ_DB_PORT, BIZ_DB_NAME, BIZ_DB_USER, BIZ_DB_PASSWORD.
Run against a backup/restored copy before a production maintenance window.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path

import pymysql

ROOT = Path(__file__).resolve().parents[1]
INVENTORY = ["biz_sales", "biz_purchase", "biz_stock", "biz_stock_move", "biz_stock_check", "biz_return"]
EMPLOYEE_REFS = ["biz_leave", "biz_leave_quota"]


def connect():
    return pymysql.connect(host=os.environ.get("BIZ_DB_HOST", "127.0.0.1"),
                           port=int(os.environ.get("BIZ_DB_PORT", "3306")),
                           user=os.environ["BIZ_DB_USER"], password=os.environ["BIZ_DB_PASSWORD"],
                           database=os.environ["BIZ_DB_NAME"], charset="utf8mb4", autocommit=True,
                           cursorclass=pymysql.cursors.DictCursor)


def query(db, sql, args=None):
    with db.cursor() as cur:
        cur.execute(sql, args)
        return cur.fetchall()


def execute(db, sql, args=None):
    with db.cursor() as cur:
        return cur.execute(sql, args)


def statements(text):
    """Split SQL outside quotes/comments, including multiline inserts and semicolons in text."""
    buf, quote, i = [], None, 0
    while i < len(text):
        ch = text[i]
        if quote:
            buf.append(ch)
            if ch == "\\" and i + 1 < len(text):
                i += 1
                buf.append(text[i])
            elif ch == quote:
                if i + 1 < len(text) and text[i + 1] == quote:
                    i += 1
                    buf.append(text[i])
                else:
                    quote = None
        elif ch in "'\"`":
            quote = ch
            buf.append(ch)
        elif text[i:i + 2] == "--" or ch == "#":
            end = text.find("\n", i)
            i = len(text) if end < 0 else end
            buf.append("\n")
        elif text[i:i + 2] == "/*":
            end = text.find("*/", i + 2)
            if end < 0:
                raise ValueError("Unterminated SQL comment")
            i = end + 1
        elif ch == ";":
            if "".join(buf).strip():
                yield "".join(buf).strip()
            buf = []
        else:
            buf.append(ch)
        i += 1
    if "".join(buf).strip():
        yield "".join(buf).strip()


def tables(db):
    return {next(iter(row.values())) for row in query(db, "SHOW TABLES")}


def column(db, table, name, definition):
    if not query(db, f"SHOW COLUMNS FROM `{table}` LIKE %s", (name,)):
        execute(db, f"ALTER TABLE `{table}` ADD COLUMN `{name}` {definition}")


def index(db, table, name, definition):
    if not query(db, f"SHOW INDEX FROM `{table}` WHERE Key_name=%s", (name,)):
        execute(db, f"ALTER TABLE `{table}` ADD {definition}")


def preflight(db, mapping):
    """Ambiguous active references stop before the first schema/data mutation."""
    issues = []
    for refs, master, field, kind in [(INVENTORY, "biz_product", "product_name", "product"),
                                      (EMPLOYEE_REFS + ["system_users"], "biz_employee", "emp_name", "employee")]:
        masters = query(db, f"SELECT id,tenant_id,`{field}` AS name FROM `{master}` WHERE deleted=0")
        lookup = {}
        for row in masters:
            lookup.setdefault((row["tenant_id"], row["name"]), []).append(row["id"])
        for table in refs:
            source = "nickname" if table == "system_users" else field
            for row in query(db, f"SELECT id,tenant_id,`{source}` AS name FROM `{table}` WHERE deleted=0"):
                key = f"{table}:{row['id']}:{kind}"
                candidates = lookup.get((row["tenant_id"], row["name"]), [])
                selected = mapping.get(key)
                if selected is not None and selected not in candidates:
                    issues.append({"key": key, "reason": "mapping does not match tenant/name", "candidates": candidates})
                elif len(candidates) > 1 and selected is None:
                    issues.append({"key": key, "name": row["name"], "tenantId": row["tenant_id"], "candidates": candidates})
    for table, group in [("biz_stock", "tenant_id,product_name,warehouse"),
                         ("biz_leave_quota", "tenant_id,emp_name,leave_type,year")]:
        duplicates = query(db, f"SELECT {group},COUNT(*) AS count FROM `{table}` WHERE deleted=0 GROUP BY {group} HAVING COUNT(*)>1")
        issues.extend({"table": table, "reason": "duplicate balance rows require reconciliation", "group": row} for row in duplicates)
    # A unique employee name is insufficient if several login accounts share it.
    # Detect this before DDL, including installations that have no employee master yet.
    employee_columns = {row['Field'] for row in query(db, 'SHOW COLUMNS FROM biz_employee')}
    employees = query(db, 'SELECT * FROM biz_employee WHERE deleted=0')
    claimed = {(row['tenant_id'], row['id']): row.get('user_id') for row in employees if row.get('user_id') is not None}
    for user in query(db, 'SELECT id,tenant_id,nickname FROM system_users WHERE deleted=0 ORDER BY id'):
        if 'user_id' in employee_columns and any(e['tenant_id']==user['tenant_id'] and e.get('user_id')==user['id'] for e in employees):
            continue
        key = f"system_users:{user['id']}:employee"
        candidates = [e['id'] for e in employees if e['tenant_id']==user['tenant_id'] and e['emp_name']==user['nickname']]
        selected = mapping.get(key)
        if selected is None and len(candidates)>1:
            continue  # Already reported above.
        identity = selected if selected is not None else (candidates[0] if candidates else ('missing',user['nickname']))
        owner_key = (user['tenant_id'], identity)
        if owner_key in claimed and claimed[owner_key] != user['id']:
            issues.append({'key':key,'reason':'multiple login accounts map to one employee; create distinct employee masters and map explicitly','otherUserId':claimed[owner_key]})
        else:
            claimed[owner_key] = user['id']
    return issues


def migrate_identity(db, mapping):
    # DDL is restartable because MySQL DDL commits implicitly. History is recorded only after verification.
    execute(db, """CREATE TABLE IF NOT EXISTS biz_warehouse (
        id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY, name varchar(64) NOT NULL,
        creator varchar(64) DEFAULT '',create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updater varchar(64) DEFAULT '',update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        deleted bit(1) NOT NULL DEFAULT b'0',tenant_id bigint NOT NULL DEFAULT 0,
        UNIQUE KEY uk_warehouse_name(tenant_id,name)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""")
    for table in INVENTORY:
        column(db, table, "product_id", "bigint NULL")
        column(db, table, "warehouse_id", "bigint NULL")
        if table in ("biz_sales", "biz_purchase"):
            column(db, table, "warehouse", "varchar(64) DEFAULT '默认仓库'")
    for table in EMPLOYEE_REFS:
        column(db, table, "employee_id", "bigint NULL")
    column(db, "biz_employee", "user_id", "bigint NULL")
    column(db, "biz_employee", "active_user_id", "bigint GENERATED ALWAYS AS (IF(deleted=0,user_id,NULL)) STORED")
    index(db, "biz_employee", "uk_employee_user", "UNIQUE KEY uk_employee_user(tenant_id,active_user_id)")

    def resolve(table, row, kind, name):
        master, field = ("biz_product", "product_name") if kind == "product" else ("biz_employee", "emp_name")
        chosen = mapping.get(f"{table}:{row['id']}:{kind}")
        if chosen:
            return chosen
        found = query(db, f"SELECT id FROM `{master}` WHERE tenant_id=%s AND `{field}`=%s AND deleted=0",
                      (row["tenant_id"], name))
        if len(found) > 1:
            raise RuntimeError(f"Ambiguous reference {table}:{row['id']}:{kind}")
        if found:
            return found[0]["id"]
        # A historical name without a master gets an explicit legacy master; no money/quantity is inferred.
        code = "product_code" if kind == "product" else "emp_no"
        import uuid
        execute(db, f"INSERT INTO `{master}`(tenant_id,`{field}`,`{code}`,status) VALUES(%s,%s,%s,'0')",
                (row["tenant_id"], name or f"历史资料#{table}:{row['id']}", "MIG" + uuid.uuid4().hex[:20]))
        return query(db, "SELECT LAST_INSERT_ID() AS id")[0]["id"]

    for row in query(db, "SELECT id,tenant_id,nickname FROM system_users WHERE deleted=0"):
        linked = query(db, "SELECT id FROM biz_employee WHERE tenant_id=%s AND user_id=%s AND deleted=0", (row["tenant_id"],row["id"]))
        if linked:
            continue
        eid = resolve("system_users", row, "employee", row["nickname"])
        existing = query(db, "SELECT user_id FROM biz_employee WHERE id=%s", (eid,))[0]["user_id"]
        if existing is not None and existing != row["id"]:
            raise RuntimeError("Two login users map to one employee; provide distinct employee masters before migration")
        execute(db, "UPDATE biz_employee SET user_id=%s WHERE id=%s", (row["id"],eid))
    for table in INVENTORY:
        for row in query(db, f"SELECT * FROM `{table}` WHERE deleted=0"):
            pid = row.get("product_id") or resolve(table, row, "product", row["product_name"])
            name = row.get("warehouse") or "默认仓库"
            execute(db, "INSERT INTO biz_warehouse(tenant_id,name) VALUES(%s,%s) ON DUPLICATE KEY UPDATE name=VALUES(name)", (row["tenant_id"],name))
            wid = query(db,"SELECT id FROM biz_warehouse WHERE tenant_id=%s AND name=%s",(row["tenant_id"],name))[0]["id"]
            execute(db, f"UPDATE `{table}` SET product_id=%s,warehouse_id=%s WHERE id=%s",(pid,wid,row["id"]))
    for row in query(db, "SELECT DISTINCT tenant_id FROM system_users UNION SELECT id AS tenant_id FROM system_tenant"):
        execute(db, "INSERT INTO biz_warehouse(tenant_id,name) VALUES(%s,'默认仓库') ON DUPLICATE KEY UPDATE name=VALUES(name)", (row["tenant_id"],))
    for table in EMPLOYEE_REFS:
        for row in query(db, f"SELECT * FROM `{table}` WHERE deleted=0 AND employee_id IS NULL"):
            eid = resolve(table,row,"employee",row["emp_name"])
            execute(db, f"UPDATE `{table}` SET employee_id=%s WHERE id=%s", (eid,row["id"]))
    for table, name, group in [("biz_stock","uk_stock_identity","tenant_id,product_id,warehouse_id,active_row"),
                                ("biz_leave_quota","uk_quota_identity","tenant_id,employee_id,leave_type,year,active_row")]:
        column(db,table,"active_row","tinyint GENERATED ALWAYS AS (IF(deleted=0,1,NULL)) STORED")
        index(db,table,name,f"UNIQUE KEY {name}({group})")
    for table in INVENTORY:
        index(db,table,"idx_inventory_identity","KEY idx_inventory_identity(tenant_id,product_id,warehouse_id)")


def migrate_finance(db):
    for name, definition in [("reversal_of_id","bigint NULL"),("source_return_id","bigint NULL"),("request_id","varchar(96) NULL")]:
        column(db,"biz_payment",name,definition)
    index(db,"biz_payment","uk_payment_request","UNIQUE KEY uk_payment_request(tenant_id,request_id)")
    index(db,"biz_payment","uk_payment_reversal","UNIQUE KEY uk_payment_reversal(tenant_id,reversal_of_id)")
    index(db,"biz_payment","idx_payment_order","KEY idx_payment_order(tenant_id,biz_type,order_id,deleted)")
    for table, field in [("biz_payment","payment_no"),("biz_return","return_no"),("biz_stock_check","check_no"),("biz_contract","contract_code"),
                         ("biz_sales","sales_code"),("biz_purchase","purchase_code")]:
        column(db,table,"legacy_document_no","varchar(128) NULL")
        execute(db, f"ALTER TABLE `{table}` MODIFY `{field}` varchar(64) NOT NULL")
        # Old second-resolution collisions retain the original in an audit column; IDs/references remain unchanged.
        rows=query(db,f"SELECT id,tenant_id,`{field}` AS code FROM `{table}` ORDER BY id")
        seen=set()
        for row in rows:
            key=(row['tenant_id'],row['code'])
            if key in seen:
                import uuid
                execute(db,f"UPDATE `{table}` SET legacy_document_no=COALESCE(legacy_document_no,`{field}`),`{field}`=%s WHERE id=%s",("MIG"+uuid.uuid4().hex,row['id']))
            else:seen.add(key)
        index(db,table,"uk_document_no",f"UNIQUE KEY uk_document_no(tenant_id,`{field}`)")
    execute(db,"UPDATE system_menu SET permission='biz:payment:reverse',name='收付款冲销' WHERE permission='biz:payment:delete'")


def run(db, apply=False, mapping=None):
    if not apply:
        return _run_locked(db, False, mapping)
    lock=query(db,"SELECT GET_LOCK(CONCAT(DATABASE(),':biz-migration'),10) AS acquired")[0]['acquired']
    if lock != 1: raise RuntimeError('Another migration is running')
    try:
        return _run_locked(db, True, mapping)
    finally:
        query(db,"SELECT RELEASE_LOCK(CONCAT(DATABASE(),':biz-migration'))")


def _run_locked(db, apply=False, mapping=None):
    mapping=mapping or {}
    existing=tables(db)
    baseline=ROOT/'sql/migrations/V001__schema.sql'
    checksum=hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
    if 'biz_schema_history' in existing:
        done=query(db,"SELECT checksum FROM biz_schema_history WHERE version='002'")
        if done:
            if done[0]['checksum'] != checksum: raise RuntimeError('Applied migration checksum changed; create a new migration version')
            print('Schema 002 already applied; no changes')
            return []
    if existing and 'biz_payment' not in existing and 'biz_schema_history' not in existing:
        raise RuntimeError('Partial/unknown schema: restore a supported 2026-09-10 baseline before upgrading')
    bootstrap = not existing or ('biz_schema_history' in existing and bool(query(db,"SELECT version FROM biz_schema_history WHERE version='000'")))
    issues=preflight(db,mapping) if existing and not bootstrap else []
    if issues or not apply:
        print('Preflight issues:',len(issues),'; planned version: 002; empty baseline:',not bool(existing))
        return issues
    execute(db,"CREATE TABLE IF NOT EXISTS biz_schema_history(version varchar(16) PRIMARY KEY,checksum char(64) NOT NULL,applied_at timestamp DEFAULT CURRENT_TIMESTAMP)")
    if bootstrap:
        execute(db,"INSERT IGNORE INTO biz_schema_history(version,checksum) VALUES('000',%s)",(hashlib.sha256(baseline.read_bytes()).hexdigest(),))
        execute(db,"SET FOREIGN_KEY_CHECKS=0")
        try:
            for stmt in statements(baseline.read_text(encoding='utf-8')):
                execute(db,stmt.replace('CREATE TABLE `','CREATE TABLE IF NOT EXISTS `',1))
            seeds=ROOT/'sql/migrations/V001__seed.sql'
            if seeds.exists():
                for stmt in statements(seeds.read_text(encoding='utf-8')):
                    execute(db,stmt + ' ON DUPLICATE KEY UPDATE id=id')
        finally:
            execute(db,"SET FOREIGN_KEY_CHECKS=1")
        execute(db,"DELETE FROM biz_schema_history WHERE version='000'")
    execute(db,"INSERT IGNORE INTO biz_schema_history(version,checksum) VALUES('001',%s)",(hashlib.sha256(baseline.read_bytes()).hexdigest(),))
    migrate_identity(db,mapping)
    migrate_finance(db)
    for table,cols in [(t,['product_id','warehouse_id']) for t in INVENTORY]+[(t,['employee_id']) for t in EMPLOYEE_REFS]:
        if query(db,f"SELECT id FROM `{table}` WHERE deleted=0 AND ("+' OR '.join(f'{c} IS NULL' for c in cols)+") LIMIT 1"):
            raise RuntimeError('Unresolved active IDs in '+table)
    execute(db,"INSERT INTO biz_schema_history(version,checksum) VALUES('002',%s)",(checksum,))
    print('Migration 002 applied and verified')
    return []


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apply',action='store_true')
    parser.add_argument('--mapping',type=Path)
    parser.add_argument('--report',type=Path,default=Path('migration-issues.json'))
    args=parser.parse_args()
    mapping=json.loads(args.mapping.read_text(encoding='utf-8')) if args.mapping else {}
    with connect() as db: issues=run(db,args.apply,mapping)
    if issues:
        args.report.write_text(json.dumps(issues,ensure_ascii=False,indent=2,default=str),encoding='utf-8')
        raise SystemExit('Migration stopped before changes; resolve the mappings in '+str(args.report))


if __name__ == '__main__':main()
