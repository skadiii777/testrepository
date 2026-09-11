"""MySQL upgrade regression: explicit disambiguation, preserved balances, numbering and repeatability."""
import sys
import os
import secrets
import subprocess
from pathlib import Path

sys.path.insert(0,str(Path(__file__).resolve().parents[1]/'tools'))
import migrate


def verify(db, database):
    assert database.startswith('enterprise_pro_qa_')
    migrate.execute(db,'CREATE DATABASE `'+database+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
    db.select_db(database)
    migrate.execute(db,'SET FOREIGN_KEY_CHECKS=0')
    try:
        for statement in migrate.statements((migrate.ROOT/'sql/migrations/V001__schema.sql').read_text(encoding='utf-8')):
            migrate.execute(db,statement)
    finally:
        migrate.execute(db,'SET FOREIGN_KEY_CHECKS=1')
    migrate.execute(db,"INSERT INTO biz_product(id,tenant_id,product_code,product_name,status) VALUES(1,1,'A','duplicate','0'),(2,1,'B','duplicate','0')")
    migrate.execute(db,"INSERT INTO biz_stock(id,tenant_id,product_name,warehouse,quantity,min_quantity) VALUES(1,1,'duplicate','warehouse',37,3)")
    migrate.execute(db,"INSERT INTO biz_payment(id,tenant_id,payment_no,payment_type,biz_type,order_id,order_code,amount,payment_date) VALUES(1,1,'SAME-SECOND','1','1',1,'OLD',12.34,'2026-09-11'),(2,1,'SAME-SECOND','1','1',1,'OLD',5,'2026-09-11')")
    issues=migrate.run(db,True)
    assert any(i.get('key')=='biz_stock:1:product' for i in issues)
    assert not migrate.query(db,"SHOW COLUMNS FROM biz_stock LIKE 'product_id'"), 'Preflight must stop before DDL'
    assert migrate.preflight(db,{'biz_stock:1:product':999}), 'Invalid/cross-tenant mappings must be rejected'
    migrate.execute(db,"INSERT INTO system_users(id,tenant_id,username,password,nickname,status) VALUES(1,1,'first','disabled','same name',1),(2,1,'second','disabled','same name',1)")
    assert any('multiple login' in issue.get('reason','') for issue in migrate.preflight(db,{'biz_stock:1:product':2}))
    assert not migrate.query(db,"SHOW COLUMNS FROM biz_employee LIKE 'user_id'"), 'Account ambiguity must stop before DDL'
    migrate.execute(db,"INSERT INTO biz_employee(id,tenant_id,emp_no,emp_name,status) VALUES(1,1,'FIRST','same name','0'),(2,1,'SECOND','same name','0')")
    mapping={'biz_stock:1:product':2,'system_users:1:employee':1,'system_users:2:employee':2}
    assert migrate.run(db,True,mapping)==[]
    assert [row['user_id'] for row in migrate.query(db,'SELECT user_id FROM biz_employee ORDER BY id')]==[1,2]
    row=migrate.query(db,'SELECT quantity,product_id,warehouse_id FROM biz_stock WHERE id=1')[0]
    assert row['quantity']==37 and row['product_id']==2 and row['warehouse_id'] is not None
    payments=migrate.query(db,'SELECT id,payment_no,legacy_document_no,amount FROM biz_payment ORDER BY id')
    assert payments[0]['payment_no']!=payments[1]['payment_no']
    assert payments[1]['legacy_document_no']=='SAME-SECOND'
    assert str(payments[0]['amount'])=='12.34' and str(payments[1]['amount'])=='5.00'
    before=migrate.query(db,'SELECT * FROM biz_payment ORDER BY id')
    migrate.run(db,True)
    assert before==migrate.query(db,'SELECT * FROM biz_payment ORDER BY id')
    print('Upgrade regression: ambiguity preflight, explicit mapping, balance preservation, legacy numbers, repeatability PASS',flush=True)


def verify_bootstrap(db, database):
    import bcrypt
    assert database.startswith('enterprise_pro_qa_')
    migrate.execute(db,'CREATE DATABASE `'+database+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
    db.select_db(database)
    migrate.run(db,True)
    migrate.run(db,True)
    env=os.environ.copy()
    env.update(BIZ_DB_NAME=database,BIZ_BOOTSTRAP_ADMIN_PASSWORD=secrets.token_urlsafe(24))
    command=[sys.executable,str(migrate.ROOT/'tools/bootstrap_admin.py')]
    result=subprocess.run(command,env=env,capture_output=True,text=True)
    assert result.returncode==0, result.stderr
    encoded=migrate.query(db,'SELECT password FROM system_users WHERE id=1')[0]['password']
    assert bcrypt.checkpw(env['BIZ_BOOTSTRAP_ADMIN_PASSWORD'].encode(),encoded.encode())
    assert migrate.query(db,'SELECT id FROM biz_employee WHERE user_id=1')
    result=subprocess.run(command,env=env,capture_output=True,text=True)
    assert result.returncode!=0 and 'Users already exist' in result.stderr
    print('Empty install, admin password, employee binding and repeated-bootstrap refusal PASS',flush=True)


if __name__=='__main__':
    import time
    with migrate.connect() as connection:
        verify(connection,'enterprise_pro_qa_upgrade_'+time.strftime('%Y%m%d_%H%M%S'))
        verify_bootstrap(connection,'enterprise_pro_qa_bootstrap_'+time.strftime('%Y%m%d_%H%M%S'))
