# -*- coding: utf-8 -*-
"""enterprise-pro 自动化冒烟测试（yudao 架构版）

与旧版（enterprise-ms/tests/smoke_test.py）对应，适配差异：
1. 接口前缀 /admin-api，返回 CommonResult{code, msg, data}，分页在 data.total/list
2. 认证改 OAuth2 token：POST /system/auth/login -> data.accessToken，
   后续请求 Header: Authorization: Bearer <token> + tenant-id: 1
3. 写操作为 JSON body（PUT/POST/DELETE），查询参数走 query string

运行前提：enterprise-server 已启动(48080)，Redis 已启动，enterprise-biz.sql 已导入
"""
import json
import socket
import ipaddress
import time
from urllib.parse import urlparse

import requests

BASE = "http://localhost:48080/admin-api"
ALLOWED_HOSTS = ("localhost", "127.0.0.1")
TENANT = "1"

S = requests.Session()
TOKEN = {"value": None}
results = []


def check(name, ok, detail=""):
    results.append((name, ok, detail))
    print("[%s] %s %s" % ("PASS" if ok else "FAIL", name, detail if not ok else ""))


def req(method, path, **kwargs):
    """统一请求入口：协议 http、主机限本地回环、自动带 token/租户头（防 SSRF）"""
    url = BASE + path
    parsed = urlparse(url)
    assert parsed.scheme == "http", "仅允许 http 协议"
    assert parsed.hostname in ALLOWED_HOSTS, "仅允许本地被测服务"
    ip = socket.gethostbyname(parsed.hostname)
    assert ipaddress.ip_address(ip).is_loopback, "解析后地址必须为回环地址"
    headers = kwargs.setdefault("headers", {})
    headers.setdefault("tenant-id", TENANT)
    if TOKEN["value"]:
        headers.setdefault("Authorization", "Bearer " + TOKEN["value"])
    return getattr(S, method)(url, **kwargs)


def jpost(path, body=None, **kw):
    return req("post", path, json=body or {}, **kw)


def jput(path, body=None, **kw):
    return req("put", path, json=body or {}, **kw)


def jdelete(path, **kw):
    return req("delete", path, **kw)


def jget(path, **kw):
    return req("get", path, **kw)


def login(username, password):
    S.cookies.clear()
    TOKEN["value"] = None
    r = jpost("/system/auth/login", {"username": username, "password": password})
    if r.json().get("code") == 0:
        TOKEN["value"] = r.json()["data"]["accessToken"]
    return r


# ---------------- 1. 登录鉴权 ----------------
def test_auth():
    r = login("admin", "wrongpass")
    check("登录-错误密码被拒绝", r.json().get("code") != 0, r.text[:100])
    r = login("admin", "admin123")
    check("登录-正确凭证通过", r.json().get("code") == 0, r.text[:100])
    TOKEN["value"] = None
    r = jget("/biz/customer/page")
    body = r.json()
    check("鉴权-无token访问被拦截", body.get("code") == 401, "HTTP %s body=%s" % (r.status_code, r.text[:80]))
    login("admin", "admin123")


# ---------------- 2. 业务模块 CRUD ----------------
CASES = {
    "customer": ("customerName", dict(customerName="测试客户", contactPerson="张三", status="0"),
                 dict(customerName="测试客户改")),
    "product": ("productName", dict(productCode="T001", productName="测试产品", category="通用",
                                    unit="个", price="10.50", status="0"),
                dict(productName="测试产品改")),
    "contract": ("contractCode", dict(contractCode="HT001", customerName="测试客户", amount="1000",
                                      signDate="2026-09-05", owner="管理员", status="0"),
                 dict(amount="2000")),
    "supplier": ("supplierName", dict(supplierName="测试供应商", contactPerson="李四", status="0"),
                 dict(supplierName="测试供应商改")),
    "employee": ("empName", dict(empNo="E9999", empName="测试员工", deptName="测试部", status="0"),
                 dict(empName="测试员工改")),
    "attendance": ("empName", dict(empName="测试员工", workDate="2026-09-05", checkIn="08:55",
                                   checkOut="18:05", status="0"),
                   dict(status="1")),
    "stock": ("productName", dict(productName="测试产品", warehouse="测试仓库", quantity=100, minQuantity=10),
              dict(quantity=150)),
}


def find_id(module, key_field, key_value):
    r = jget("/biz/%s/page" % module, params={key_field: key_value, "pageNo": 1, "pageSize": 10})
    for row in r.json().get("data", {}).get("list", []):
        if row.get(key_field) == key_value:
            return row.get("id")
    return None


def crud_flow(module, key_field, add_data, edit_map):
    r = jget("/biz/%s/page" % module, params={"pageNo": 1, "pageSize": 10})
    check("%s-分页查询" % module, r.json().get("code") == 0 and "total" in r.json().get("data", {}), r.text[:120])
    r = jpost("/biz/%s/create" % module, add_data)
    check("%s-新增" % module, r.json().get("code") == 0, r.text[:150])
    rid = find_id(module, key_field, add_data[key_field])
    check("%s-新增后可查询" % module, rid is not None, "未找到 %s=%s" % (key_field, add_data[key_field]))
    if rid is None:
        return None
    r = jget("/biz/%s/get" % module, params={"id": rid})
    check("%s-详情" % module, r.json().get("code") == 0, r.text[:120])
    if edit_map:
        edit = dict(add_data)
        edit.update(edit_map)
        edit["id"] = rid
        r = jput("/biz/%s/update" % module, edit)
        check("%s-修改" % module, r.json().get("code") == 0, r.text[:150])
    return rid


def delete(module, rid):
    if rid is None:
        return
    r = jdelete("/biz/%s/delete" % module, params={"id": rid})
    check("%s-删除" % module, r.json().get("code") == 0, r.text[:120])


def test_crud():
    for module, (key_field, add_data, edit_map) in CASES.items():
        rid = crud_flow(module, key_field, add_data, edit_map)
        delete(module, rid)


# ---------------- 3. 业务规则：出入库/流水 ----------------
TAG = "规则测试%d" % (int(time.time()) % 1000000)


def get_stock(product):
    r = jget("/biz/stock/page", params={"productName": product, "pageNo": 1, "pageSize": 10})
    rows = r.json().get("data", {}).get("list", [])
    return rows[0]["quantity"] if rows else 0


def test_rules():
    jpost("/biz/product/create", dict(productCode="R001", productName=TAG, category="测试",
                                      unit="个", price=5, status="0"))
    before = get_stock(TAG)

    # 状态机：草稿(创建) -> 确认 -> 完成(入库) ；作废分支；非法流转
    r = jpost("/biz/purchase/create", dict(purchaseCode="CG" + TAG[-6:], supplierName="规则供应商",
              productName=TAG, quantity=50, price=3, purchaseDate="2026-09-05"))
    check("状态机-采购单默认草稿", r.json().get("code") == 0, r.text[:150])
    pid = find_id("purchase", "purchaseCode", "CG" + TAG[-6:])
    mid = get_stock(TAG)
    check("状态机-草稿不改库存", mid == before, "库存 %s -> %s" % (before, mid))

    r = jpost("/biz/purchase/complete", params={"id": pid})
    check("状态机-草稿直接完成被拒", r.json().get("code") != 0, r.text[:120])

    r = jpost("/biz/purchase/transition", params={"id": pid, "action": "confirm"})
    check("状态机-确认成功", r.json().get("code") == 0, r.text[:120])
    r = jpost("/biz/purchase/transition", params={"id": pid, "action": "confirm"})
    check("状态机-重复确认被拒", r.json().get("code") != 0, r.text[:120])

    r = jpost("/biz/purchase/complete", params={"id": pid})
    check("状态机-完成采购自动入库", r.json().get("code") == 0, r.text[:120])
    after = get_stock(TAG)
    check("规则-采购入库自动加库存", after == mid + 50, "库存 %s -> %s" % (mid, after))

    r = jpost("/biz/purchase/transition", params={"id": pid, "action": "void"})
    check("状态机-已完成不可作废", r.json().get("code") != 0, r.text[:120])

    # 销售单：完成时出库，库存不足拒绝且状态回滚
    r = jpost("/biz/sales/create", dict(salesCode="XS" + TAG[-6:] + "A", customerName="规则客户",
              productName=TAG, quantity=99999, price=5, salesDate="2026-09-05"))
    check("状态机-销售单默认草稿", r.json().get("code") == 0, r.text[:150])
    sid_a = find_id("sales", "salesCode", "XS" + TAG[-6:] + "A")
    jpost("/biz/sales/transition", params={"id": sid_a, "action": "confirm"})
    r = jpost("/biz/sales/complete", params={"id": sid_a})
    msg = r.json().get("msg", "")
    check("状态机-完成销售库存不足被拒", r.json().get("code") != 0 and "库存不足" in msg, r.text[:150])
    r = jget("/biz/sales/get", params={"id": sid_a})
    check("状态机-失败流转状态回滚(仍已确认)", r.json().get("data", {}).get("status") == "1", r.text[:150])
    r = jpost("/biz/sales/transition", params={"id": sid_a, "action": "void"})
    check("状态机-已确认可作废", r.json().get("code") == 0, r.text[:120])

    r = jpost("/biz/sales/create", dict(salesCode="XS" + TAG[-6:] + "B", customerName="规则客户",
              productName=TAG, quantity=20, price=5, salesDate="2026-09-05"))
    sid_b = find_id("sales", "salesCode", "XS" + TAG[-6:] + "B")
    jpost("/biz/sales/transition", params={"id": sid_b, "action": "confirm"})
    r = jpost("/biz/sales/complete", params={"id": sid_b})
    check("状态机-完成销售自动出库", r.json().get("code") == 0, r.text[:120])
    after2 = get_stock(TAG)
    check("规则-销售出库自动扣库存", after2 == after - 20, "库存 %s -> %s" % (after, after2))

    # 流水溯源：最新出库带销售单号，结余连续
    r = jget("/biz/stockmove/page", params={"productName": TAG, "pageNo": 1, "pageSize": 10})
    rows = r.json().get("data", {}).get("list", [])
    check("流水-销售出库带单号", rows and rows[0]["sourceType"] == "sales"
          and rows[0]["moveType"] == "2" and rows[0]["sourceCode"] == "XS" + TAG[-6:] + "B", str(rows[:1]))

    # 清理源单与产品（流水保留）
    for mod, field, val in [("purchase", "purchaseCode", "CG" + TAG[-6:]),
                            ("sales", "salesCode", "XS" + TAG[-6:] + "A"),
                            ("sales", "salesCode", "XS" + TAG[-6:] + "B")]:
        rid = find_id(mod, field, val)
        if rid:
            jdelete("/biz/%s/delete" % mod, params={"id": rid})
    pid2 = find_id("product", "productName", TAG)
    if pid2:
        jdelete("/biz/product/delete", params={"id": pid2})


# ---------------- 4. 看板/预警/导出 ----------------
def test_dashboard():
    r = jpost("/biz/dashboard/panel")
    check("看板-指标接口", r.json().get("code") == 0 and "customerCount" in r.text, r.text[:100])
    r = jpost("/biz/dashboard/trend")
    d = r.json().get("data", {})
    check("看板-近7日趋势(7点+零填充)",
          r.json().get("code") == 0 and len(d.get("dates", [])) == 7 and len(d.get("sales", [])) == 7, r.text[:150])
    r = jpost("/biz/dashboard/productTop")
    check("看板-产品Top5", r.json().get("code") == 0, r.text[:100])
    r = jpost("/biz/dashboard/status")
    check("看板-状态分布", r.json().get("code") == 0 and "contract" in r.text, r.text[:100])

    # 库存预警闭环
    r = jpost("/biz/stock/create", dict(productName="预警产品" + TAG[-4:], warehouse="默认仓库",
                                        quantity=1, minQuantity=10))
    r = jpost("/biz/dashboard/panel")
    low = r.json().get("data", {}).get("lowStockCount", 0)
    check("预警-低库存触发", low >= 1, "lowStockCount=%s" % low)
    rid = find_id("stock", "productName", "预警产品" + TAG[-4:])
    if rid:
        jput("/biz/stock/update", dict(id=rid, productName="预警产品" + TAG[-4:], warehouse="默认仓库",
                                       quantity=100, minQuantity=10))
        r = jpost("/biz/dashboard/panel")
        check("预警-补货解除", r.json().get("data", {}).get("lowStockCount", 0) == 0, r.text[:120])
        jdelete("/biz/stock/delete", params={"id": rid})

    for name in ("customer", "product"):
        r = jget("/biz/%s/export-excel" % name, params={"pageNo": 1, "pageSize": 10})
        check("导出-%s xlsx" % name, r.status_code == 200 and r.content[:2] == b"PK",
              "HTTP %s head=%s" % (r.status_code, r.content[:4]))


# ---------------- 5. 员工工作台 ----------------
def test_portal():
    r = jget("/portal/index-data")
    check("门户-工作台数据", r.json().get("code") == 0 and "workStart" in r.text, r.text[:120])

    r = jpost("/portal/punch", params={"type": "in"})
    ok = r.json().get("code") == 0 or "无需重复" in r.json().get("msg", "")
    check("门户-上班打卡(或当日已打)", ok, r.text[:120])
    r = jpost("/portal/punch", params={"type": "in"})
    check("门户-重复上班打卡被拒", r.json().get("code") != 0, r.text[:120])
    r = jpost("/portal/punch", params={"type": "out"})
    ok = r.json().get("code") == 0 or "无需重复" in r.json().get("msg", "")
    check("门户-下班打卡(或当日已打)", ok, r.text[:120])
    r = jpost("/portal/punch", params={"type": "bad"})
    check("门户-非法打卡类型被拒", r.json().get("code") != 0, r.text[:120])

    # 请假 + 审批 + 销假（调休，未配置配额=不限额）
    r = jget("/portal/leave-page", params={"pageNo": 1, "pageSize": 20})
    for row in r.json().get("data", {}).get("list", []):
        if "门户测试" in (row.get("reason") or ""):
            jdelete("/biz/leave/delete", params={"id": row["id"]})
    r = jpost("/portal/leave-submit", dict(leaveType="4", startDate="2026-09-10",
                                           endDate="2026-09-11", days=2, reason="门户测试"))
    check("门户-提交请假", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/leave-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if "门户测试" in (x.get("reason") or "")]
    check("门户-请假默认待审批", rows and rows[0].get("status") == "0", str(rows[:1]))
    lid = rows[0]["id"] if rows else None
    r = jpost("/biz/leave/audit", params={"id": lid, "status": "1", "auditRemark": "同意"})
    check("门户-管理员审批通过", r.json().get("code") == 0, r.text[:120])
    r = jpost("/portal/leave-cancel", params={"id": lid})
    check("门户-员工销假", r.json().get("code") == 0, r.text[:120])
    r = jpost("/portal/leave-cancel", params={"id": lid})
    check("门户-重复销假被拒", r.json().get("code") != 0, r.text[:120])
    jdelete("/biz/leave/delete", params={"id": lid})

    # 汇报
    r = jpost("/portal/report-submit", dict(reportType="1", reportDate="2026-09-05",
                                            title="门户测试日报", content="完成迁移验证"))
    check("门户-提交日报", r.json().get("code") == 0, r.text[:120])
    r = jget("/portal/report-page", params={"pageNo": 1, "pageSize": 10})
    rows = [x for x in r.json().get("data", {}).get("list", []) if x.get("title") == "门户测试日报"]
    check("门户-汇报列表可见", len(rows) == 1, r.text[:120])
    if rows:
        r = jdelete("/portal/report-delete", params={"id": rows[0]["id"]})
        check("门户-删除汇报", r.json().get("code") == 0, r.text[:100])


# ---------------- 6. 假期余额 ----------------
QNICK = None  # 登录用户昵称，测试中获取


def get_nickname():
    global QNICK
    if QNICK is None:
        r = jget("/system/user/get", params={"id": 1})
        QNICK = r.json().get("data", {}).get("nickname", "管理员")
    return QNICK


def test_quota():
    nick = get_nickname()
    # 清残留
    r = jget("/biz/quota/page", params={"pageNo": 1, "pageSize": 50})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/quota/delete", params={"id": row["id"]})
    r = jget("/portal/leave-page", params={"pageNo": 1, "pageSize": 50})
    for row in r.json().get("data", {}).get("list", []):
        if "余额测试" in (row.get("reason") or ""):
            jdelete("/biz/leave/delete", params={"id": row["id"]})

    r = jpost("/biz/quota/create", dict(empName=nick, leaveType="3", year="2026", quotaDays=5))
    check("余额-新增配额(年假5天)", r.json().get("code") == 0, r.text[:150])
    r = jpost("/biz/quota/create", dict(empName=nick, leaveType="3", year="2026", quotaDays=9))
    check("余额-重复配额被拒", r.json().get("code") != 0, r.text[:150])
    qid = find_id("quota", "empName", nick)

    r = jpost("/portal/leave-submit", dict(leaveType="3", startDate="2026-10-10",
                                           endDate="2026-10-15", days=6, reason="余额测试"))
    check("余额-超余额请假被拒", r.json().get("code") != 0 and "余额不足" in r.json().get("msg", ""), r.text[:150])
    r = jpost("/portal/leave-submit", dict(leaveType="3", startDate="2026-10-10",
                                           endDate="2026-10-11", days=2, reason="余额测试"))
    check("余额-余额内请假可提交", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/leave-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if "余额测试" in (x.get("reason") or "")]
    lid = rows[0]["id"] if rows else None
    r = jpost("/biz/leave/audit", params={"id": lid, "status": "1", "auditRemark": "同意"})
    check("余额-审批通过并扣减", r.json().get("code") == 0, r.text[:150])
    r = jget("/biz/quota/page", params={"pageNo": 1, "pageSize": 10})
    rows = r.json().get("data", {}).get("list", [])
    used = float(rows[0]["usedDays"]) if rows else -1
    check("余额-已用天数=2", used == 2.0, "usedDays=%s" % used)
    r = jpost("/portal/leave-cancel", params={"id": lid})
    check("余额-销假成功", r.json().get("code") == 0, r.text[:120])
    r = jget("/biz/quota/page", params={"pageNo": 1, "pageSize": 10})
    rows = r.json().get("data", {}).get("list", [])
    used = float(rows[0]["usedDays"]) if rows else -1
    check("余额-销假后返还(已用=0)", used == 0.0, "usedDays=%s" % used)
    r = jput("/biz/quota/update", dict(id=qid, empName=nick, leaveType="3", year="2026",
                                       quotaDays=5, usedDays=6))
    check("余额-已用超配额被拒", r.json().get("code") != 0, r.text[:150])
    r = jget("/biz/quota/export-excel", params={"pageNo": 1, "pageSize": 10})
    check("余额-导出 xlsx", r.status_code == 200 and r.content[:2] == b"PK",
          "HTTP %s head=%s" % (r.status_code, r.content[:4]))
    if qid:
        jdelete("/biz/quota/delete", params={"id": qid})
    jdelete("/biz/leave/delete", params={"id": lid})


# ---------------- 7. 费用报销 + 审批中心 ----------------
def test_expense():
    reason_tag = "报销测试%d" % (int(time.time()) % 1000000)
    r = jpost("/portal/expense-submit", dict(category="1", amount=580.5,
                                             expenseDate="2026-09-05", reason=reason_tag))
    check("报销-提交", r.json().get("code") == 0, r.text[:120])
    r = jpost("/portal/expense-submit", dict(category="2", amount=0,
                                             expenseDate="2026-09-05", reason="零金额"))
    check("报销-零金额被拒", r.json().get("code") != 0, r.text[:120])
    r = jget("/portal/expense-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if x.get("reason") == reason_tag]
    check("报销-列表可见", len(rows) == 1, r.text[:150])
    eid = rows[0]["id"] if rows else None
    check("报销-默认待审批", rows and rows[0].get("status") == "0", str(rows[:1]))

    r = jpost("/biz/approval/pending")
    check("审批中心-待办角标含报销数", r.json().get("code") == 0 and "expenseCount" in r.text, r.text[:100])
    r = jget("/biz/approval/expense-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if str(x.get("id")) == str(eid)]
    check("审批中心-报销进入待办", len(rows) == 1, str(rows[:1]))
    r = jpost("/biz/approval/expense-audit", params={"id": eid, "status": "1", "auditRemark": "报销测试通过"})
    check("审批中心-报销审批通过", r.json().get("code") == 0, r.text[:120])
    r = jget("/portal/expense-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if x.get("reason") == reason_tag]
    check("报销-审批后状态与审批人",
          rows and rows[0].get("status") == "1" and rows[0].get("auditBy") == "1", str(rows[:1]))
    r = jdelete("/portal/expense-withdraw", params={"id": eid})
    check("报销-已审批不能撤回", r.json().get("code") != 0, r.text[:120])
    r = jpost("/biz/approval/expense-audit", params={"id": eid, "status": "9"})
    check("审批中心-非法状态被拒", r.json().get("code") != 0, r.text[:120])


# ---------------- 8. 系统模块 ----------------
def test_system():
    for name in ("user", "role", "dict-type"):
        r = jget("/system/%s/page" % name, params={"pageNo": 1, "pageSize": 5})
        check("系统-%s分页" % name, r.json().get("code") == 0, r.text[:100])
    r = jget("/system/dept/list")
    check("系统-部门列表", r.json().get("code") == 0, r.text[:100])


# ---------------- 10. 补卡申请（审批通过自动回写考勤） ----------------
def test_correction():
    # 清残留：撤回本人待审批的补卡
    r = jget("/portal/correction-page", params={"pageNo": 1, "pageSize": 20})
    for row in r.json().get("data", {}).get("list", []):
        if "补卡测试" in (row.get("reason") or ""):
            jdelete("/portal/correction-withdraw", params={"id": row["id"]})
    # 清测试日期的考勤
    r = jget("/biz/attendance/page", params={"empName": "管理员", "workDate": "2026-09-06", "pageNo": 1, "pageSize": 5})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/attendance/delete", params={"id": row["id"]})

    r = jpost("/portal/correction-submit", dict(workDate="2026-09-06", correctType="1",
                                               correctTime="08:50", reason="补卡测试：漏打上班卡"))
    check("补卡-提交", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/correction-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if "补卡测试" in (x.get("reason") or "")]
    check("补卡-列表可见且默认待审批", rows and rows[0].get("status") == "0", str(rows[:1]))
    cid = rows[0]["id"] if rows else None

    r = jpost("/biz/approval/pending")
    check("补卡-审批中心角标含补卡数",
          r.json().get("code") == 0 and r.json().get("data", {}).get("correctionCount", 0) >= 1, r.text[:120])
    r = jget("/biz/approval/correction-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if str(x.get("id")) == str(cid)]
    check("补卡-进入审批待办", len(rows) == 1, str(rows[:1]))

    r = jpost("/biz/approval/correction-audit", params={"id": cid, "status": "1", "auditRemark": "情况属实"})
    check("补卡-审批通过", r.json().get("code") == 0, r.text[:120])

    # 回写考勤：自动创建 2026-09-06 的考勤，check_in=08:50，状态正常
    r = jget("/biz/attendance/page", params={"empName": "管理员", "workDate": "2026-09-06", "pageNo": 1, "pageSize": 5})
    rows = r.json().get("data", {}).get("list", [])
    check("补卡-自动回写考勤记录",
          rows and rows[0].get("checkIn") == "08:50" and rows[0].get("status") == "0", str(rows[:1]))

    # 再补下班卡 16:00（走第二笔审批）→ 应重算为早退
    r = jpost("/portal/correction-submit", dict(workDate="2026-09-06", correctType="2",
                                                correctTime="16:00", reason="补卡测试：提前返家"))
    check("补卡-第二笔提交", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/correction-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", [])
            if "提前返家" in (x.get("reason") or "") and x.get("status") == "0"]
    cid2 = rows[0]["id"] if rows else None
    r = jpost("/biz/approval/correction-audit", params={"id": cid2, "status": "1", "auditRemark": "属实"})
    check("补卡-第二笔审批通过", r.json().get("code") == 0, r.text[:120])
    r = jget("/biz/attendance/page", params={"empName": "管理员", "workDate": "2026-09-06", "pageNo": 1, "pageSize": 5})
    rows = r.json().get("data", {}).get("list", [])
    check("补卡-下班早退重算", rows and rows[0].get("checkOut") == "16:00" and rows[0].get("status") == "2",
          str(rows[:1]))

    r = jpost("/biz/approval/correction-audit", params={"id": cid, "status": "1"})
    check("补卡-重复审批被拒", r.json().get("code") != 0, r.text[:120])
    r = jdelete("/portal/correction-withdraw", params={"id": cid})
    check("补卡-已审批不能撤回", r.json().get("code") != 0, r.text[:120])
    r = jpost("/biz/approval/correction-audit", params={"id": cid, "status": "9"})
    check("补卡-非法状态被拒", r.json().get("code") != 0, r.text[:120])


# ---------------- 11. 加班时长（打卡/补卡自动累计 + 月报汇总） ----------------
def test_overtime():
    # 补下班卡 20:30 -> 加班 150 分钟自动写入考勤
    r = jpost("/portal/correction-submit", dict(workDate="2026-09-07", correctType="2",
                                               correctTime="20:30", reason="加班测试：项目上线"))
    check("加班-补下班卡提交", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/correction-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", [])
            if "加班测试" in (x.get("reason") or "") and x.get("status") == "0"]
    cid = rows[0]["id"] if rows else None
    r = jpost("/biz/approval/correction-audit", params={"id": cid, "status": "1", "auditRemark": "属实"})
    check("加班-补卡审批通过", r.json().get("code") == 0, r.text[:120])
    r = jget("/biz/attendance/page", params={"empName": "管理员", "workDate": "2026-09-07", "pageNo": 1, "pageSize": 5})
    rows = r.json().get("data", {}).get("list", [])
    ot = rows[0].get("overtimeMinutes") if rows else None
    check("加班-补卡写入150分钟", rows and ot == 150, str(rows[:1]))

    # 月报：2026-09 该员工加班累计 150 分钟（本月考勤天数 >= 1，与其他用例共用月份）
    r = jget("/biz/attendance/summary", params={"month": "2026-09"})
    rows = [x for x in r.json().get("data", []) if x.get("empName") == "管理员"]
    check("加班-月报汇总(加班150)",
          rows and int(rows[0].get("overtimeMinutes")) == 150 and rows[0].get("attendDays") >= 1,
          str(rows[:1]))

    # 工作台数据含本月加班分钟
    r = jget("/portal/index-data")
    check("加班-工作台显示本月加班",
          r.json().get("code") == 0 and "monthOvertimeMinutes" in r.text, r.text[:120])

    # 清理
    r = jget("/biz/attendance/page", params={"empName": "管理员", "workDate": "2026-09-07", "pageNo": 1, "pageSize": 5})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/attendance/delete", params={"id": row["id"]})
    r = jget("/portal/correction-page", params={"pageNo": 1, "pageSize": 20})
    for row in r.json().get("data", {}).get("list", []):
        if "加班测试" in (row.get("reason") or ""):
            jdelete("/biz/correction/delete", params={"id": row["id"]})


# ---------------- 12. 客户跟进记录（时间线 + 按客户查询） ----------------
def test_followup():
    tag = "跟进测试%d" % (int(time.time()) % 1000000)
    # 建个客户
    r = jpost("/biz/customer/create", dict(customerName=tag, contactPerson="跟进联系人", status="0"))
    check("跟进-客户创建", r.json().get("code") == 0, r.text[:120])

    # 创建两条跟进（方式不同、时间不同）
    r = jpost("/biz/followup/create", dict(customerName=tag, followTime="2026-09-05 10:00",
                                           method="1", content="电话沟通需求"))
    check("跟进-电话跟进创建", r.json().get("code") == 0, r.text[:120])
    r = jpost("/biz/followup/create", dict(customerName=tag, followTime="2026-09-06 14:30",
                                           method="2", content="上门演示产品", nextDate="2026-09-10"))
    check("跟进-上门跟进创建", r.json().get("code") == 0, r.text[:120])

    # 分页查询
    r = jget("/biz/followup/page", params={"customerName": tag, "pageNo": 1, "pageSize": 10})
    check("跟进-分页可见", r.json().get("data", {}).get("total") == 2, r.text[:150])

    # 按客户查询（时间倒序：上门在前）
    r = jget("/biz/followup/list-by-customer", params={"customerName": tag})
    rows = r.json().get("data", [])
    check("跟进-按客户查询时间倒序", len(rows) == 2 and rows[0]["method"] == "2", str(rows[:2]))

    # 删除 + 确认
    fid = rows[0]["id"]
    r = jdelete("/biz/followup/delete", params={"id": fid})
    check("跟进-删除", r.json().get("code") == 0, r.text[:120])
    r = jget("/biz/followup/list-by-customer", params={"customerName": tag})
    check("跟进-删除后剩1条", len(r.json().get("data", [])) == 1, r.text[:150])

    # 清理
    r = jget("/biz/followup/page", params={"customerName": tag, "pageNo": 1, "pageSize": 10})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/followup/delete", params={"id": row["id"]})
    cid = find_id("customer", "customerName", tag)
    if cid:
        jdelete("/biz/customer/delete", params={"id": cid})


# ---------------- 13. 周报摘要（手动触发 -> 站内信落库） ----------------
def test_digest():
    r = jpost("/biz/dashboard/weekly-digest")
    check("周报-手动触发成功", r.json().get("code") == 0 and "周报" in r.text, r.text[:200])
    # 站内信落库：查我的未读站内信
    r = jget("/system/notify-message/page", params={"pageNo": 1, "pageSize": 5, "readStatus": False})
    rows = r.json().get("data", {}).get("list", [])
    hit = [x for x in rows if "上周经营摘要" in (x.get("templateContent") or x.get("content") or "")]
    check("周报-站内信已落库", len(hit) >= 1, r.text[:200])


# ---------------- 14. 文件上传 + 报销发票附件 ----------------
def test_file_upload():
    # 1) 上传一张 1x1 PNG 到 /infra/file/upload
    png = bytes.fromhex('89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c4890000000d49444154789c6260000000060005' + '27de4bb00000000049454e44ae426082')
    r = jpost("/infra/file/upload",
              files={"file": ("invoice-test.png", png, "image/png")},
              data={"path": "smoke-test"})
    check("文件-上传成功", r.json().get("code") == 0, r.text[:150])
    file_url = r.json().get("data", "")
    check("文件-返回可访问URL", file_url.startswith("http"), file_url[:120])
    r2 = requests.get(file_url, timeout=10)
    check("文件-URL可访问且内容一致", r2.status_code == 200 and r2.content[:4] == bytes.fromhex("89504e47"), "HTTP %s" % r2.status_code)

    # 2) 报销单挂发票附件
    reason_tag = "发票测试%d" % (int(time.time()) % 1000000)
    r = jpost("/portal/expense-submit", dict(category="3", amount=99.9, expenseDate="2026-09-05",
                                             reason=reason_tag, invoiceUrl=file_url))
    check("报销-带发票附件提交", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/expense-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if x.get("reason") == reason_tag]
    check("报销-附件URL已保存", rows and rows[0].get("invoiceUrl") == file_url, str(rows[:1]))
    eid = rows[0]["id"] if rows else None

    # 清理
    if eid:
        jdelete("/portal/expense-withdraw", params={"id": eid})


# ---------------- 15. BPM 工作流引擎（模型部署 -> OA 请假 -> 审批 -> 状态流转） ----------------
BPMN_XML = """<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"   xmlns:flowable="http://flowable.org/bpmn"   targetNamespace="http://flowable.org/bpmn"   typeLanguage="http://www.w3.org/2001/XMLSchema"   expressionLanguage="http://www.w3.org/1999/XPath">
  <process id="oa_leave" name="OA请假示例" isExecutable="true">
    <startEvent id="start" name="开始"/>
    <sequenceFlow id="f1" sourceRef="start" targetRef="approve"/>
    <userTask id="approve" name="审批" flowable:candidateStrategy="30" flowable:candidateParam="1"/>
    <sequenceFlow id="f2" sourceRef="approve" targetRef="end"/>
    <endEvent id="end" name="结束"/>
  </process>
</definitions>"""


def test_bpm():
    # 1) 创建 + 部署流程模型
    r = jpost("/bpm/model/create", dict(key="oa_leave", name="OA请假示例", bpmnXml=BPMN_XML,
              type=10, formType=20, visible=True, description="审批中心示例流程",
              formCustomCreatePath="/bpm/oa/leave/create", formCustomViewPath="/bpm/oa/leave/detail",
              managerUserIds=[1]))
    body = r.json()
    exists = "已经存在" in body.get("msg", "")
    check("BPM-模型创建(或已存在)", body.get("code") == 0 or exists, r.text[:150])
    r = jget("/bpm/model/list", params={"name": "OA请假示例"})
    rows = r.json().get("data", [])
    check("BPM-模型已创建", len(rows) >= 1, str(rows[:1]))
    model_id = rows[0]["id"] if rows else None
    # 已存在则更新 BPMN 内容（保证最新 XML）
    if exists and model_id:
        r = jput("/bpm/model/update", dict(id=model_id, key="oa_leave", name="OA请假示例",
                  bpmnXml=BPMN_XML, type=10, formType=20, visible=True,
                  description="审批中心示例流程", formCustomCreatePath="/bpm/oa/leave/create",
                  formCustomViewPath="/bpm/oa/leave/detail", managerUserIds=[1]))
        check("BPM-模型更新XML", r.json().get("code") == 0, r.text[:150])
    # 若未部署过，部署
    r = jget("/bpm/process-definition/simple-list")
    deployed = [d for d in r.json().get("data", []) if d.get("key") == "oa_leave"]
    if not deployed:
        r = jpost("/bpm/model/deploy", params={"id": model_id})
        check("BPM-模型部署", r.json().get("code") == 0, r.text[:150])
        r = jget("/bpm/process-definition/simple-list")
        deployed = [d for d in r.json().get("data", []) if d.get("key") == "oa_leave"]
    check("BPM-流程定义存在", len(deployed) >= 1, str(deployed[:1]))

    # 2) 发起 OA 请假（走工作流）
    r = jpost("/bpm/oa/leave/create", dict(startTime="2026-09-10 09:00:00",
              endTime="2026-09-11 18:00:00", type=1, reason="BPM流程测试"))
    check("BPM-OA请假发起", r.json().get("code") == 0, r.text[:150])
    r = jget("/bpm/oa/leave/page", params={"pageNo": 1, "pageSize": 10})
    rows = r.json().get("data", {}).get("list", [])
    mine = [x for x in rows if x.get("reason") == "BPM流程测试"]
    check("BPM-OA请假单状态=审批中", mine and mine[0].get("status") == 1, str(mine[:1]))
    oid = mine[0]["id"] if mine else None
    proc_id = mine[0].get("processInstanceId") if mine else None

    # 3) 管理员审批通过
    r = jget("/bpm/task/todo-page", params={"pageNo": 1, "pageSize": 10})
    tasks = r.json().get("data", {}).get("list", [])
    task = [t for t in tasks if t.get("processInstanceId") == proc_id]
    check("BPM-待办任务存在", len(task) == 1, str(task[:1]))
    if task:
        r = jput("/bpm/task/approve", dict(id=task[0]["id"], reason="同意"))
        check("BPM-审批通过", r.json().get("code") == 0, r.text[:150])
        r = jget("/bpm/oa/leave/page", params={"pageNo": 1, "pageSize": 10})
        rows = [x for x in r.json().get("data", {}).get("list", []) if x.get("id") == oid]
        check("BPM-请假单状态=已通过(2)", rows and rows[0].get("status") == 2, str(rows[:1]))


# ---------------- 16. 请假接入 Flowable（biz_leave 流程模型 -> 发起 -> 任务审批 -> 状态回调） ----------------
LEAVE_BPMN = """<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"   xmlns:flowable="http://flowable.org/bpmn"   targetNamespace="http://flowable.org/bpmn"   typeLanguage="http://www.w3.org/2001/XMLSchema"   expressionLanguage="http://www.w3.org/1999/XPath">
  <process id="biz_leave" name="请假审批流程" isExecutable="true">
    <startEvent id="start" name="发起"/>
    <sequenceFlow id="f1" sourceRef="start" targetRef="mgr"/>
    <userTask id="mgr" name="部门经理审批" flowable:candidateStrategy="30" flowable:candidateParam="1"/>
    <sequenceFlow id="f2" sourceRef="mgr" targetRef="end"/>
    <endEvent id="end" name="结束"/>
  </process>
</definitions>"""


def deploy_biz_leave_model():
    """部署（或更新）biz_leave 流程模型，幂等"""
    r = jpost("/bpm/model/create", dict(key="biz_leave", name="请假审批流程", bpmnXml=LEAVE_BPMN,
              type=10, formType=20, visible=True, description="业务请假：审批通过自动扣余额",
              formCustomCreatePath="/portal/leave", formCustomViewPath="/portal/leave",
              managerUserIds=[1]))
    body = r.json()
    exists = "已经存在" in body.get("msg", "")
    r = jget("/bpm/model/list", params={"name": "请假审批流程"})
    rows = [m for m in r.json().get("data", []) if m["key"] == "biz_leave"]
    mid = rows[0]["id"] if rows else None
    if exists and mid:
        jput("/bpm/model/update", dict(id=mid, key="biz_leave", name="请假审批流程",
             bpmnXml=LEAVE_BPMN, type=10, formType=20, visible=True,
             description="业务请假：审批通过自动扣余额",
             formCustomCreatePath="/portal/leave", formCustomViewPath="/portal/leave",
             managerUserIds=[1]))
    r = jpost("/bpm/model/deploy", params={"id": mid})
    return r.json().get("code") == 0


def test_leave_workflow():
    ok = deploy_biz_leave_model()
    check("请假流程-模型部署", ok, "deploy failed")

    # 清残留
    r = jget("/portal/leave-page", params={"pageNo": 1, "pageSize": 50})
    for row in r.json().get("data", {}).get("list", []):
        if "流程测试" in (row.get("reason") or ""):
            jdelete("/biz/leave/delete", params={"id": row["id"]})
    r = jget("/biz/quota/page", params={"pageNo": 1, "pageSize": 50})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/quota/delete", params={"id": row["id"]})
    jpost("/biz/quota/create", dict(empName="管理员", leaveType="3", year="2026", quotaDays=5))

    # 1) 提交请假 -> 应自动发起 BPM 流程
    r = jpost("/portal/leave-submit", dict(leaveType="3", startDate="2026-11-10",
                                           endDate="2026-11-11", days=2, reason="流程测试：走BPM"))
    check("请假流程-提交", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/leave-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if "流程测试" in (x.get("reason") or "")]
    check("请假流程-流程实例已关联", rows and rows[0].get("processInstanceId"), str(rows[:1]))
    lid = rows[0]["id"] if rows else None
    proc_id = rows[0].get("processInstanceId") if rows else None

    # 2) 待办任务出现并审批通过
    r = jget("/bpm/task/todo-page", params={"pageNo": 1, "pageSize": 20})
    tasks = [t for t in r.json().get("data", {}).get("list", []) if t.get("processInstanceId") == proc_id]
    check("请假流程-部门经理待办", len(tasks) == 1, str(tasks[:1]))
    if tasks:
        r = jput("/bpm/task/approve", dict(id=tasks[0]["id"], reason="同意（流程测试）"))
        check("请假流程-BPM审批通过", r.json().get("code") == 0, r.text[:150])

    # 3) 状态事件回调 -> 请假单已通过 + 余额已扣
    r = jget("/portal/leave-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if x.get("id") == lid]
    check("请假流程-状态回调为已通过(1)", rows and rows[0].get("status") == "1", str(rows[:1]))
    r = jget("/biz/quota/page", params={"pageNo": 1, "pageSize": 10})
    qrows = r.json().get("data", {}).get("list", [])
    used = float(qrows[0]["usedDays"]) if qrows else -1
    check("请假流程-余额已扣(2天)", used == 2.0, "usedDays=%s" % used)

    # 清理
    if lid:
        jdelete("/biz/leave/delete", params={"id": lid})
    r = jget("/biz/quota/page", params={"pageNo": 1, "pageSize": 10})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/quota/delete", params={"id": row["id"]})


# ---------------- 17. 报销接入 Flowable（biz_expense 流程模型 -> 发起 -> 审批 -> 状态回调） ----------------
EXPENSE_BPMN = """<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"   xmlns:flowable="http://flowable.org/bpmn"   targetNamespace="http://flowable.org/bpmn"   typeLanguage="http://www.w3.org/2001/XMLSchema"   expressionLanguage="http://www.w3.org/1999/XPath">
  <process id="biz_expense" name="报销审批流程" isExecutable="true">
    <startEvent id="start" name="发起"/>
    <sequenceFlow id="f1" sourceRef="start" targetRef="mgr"/>
    <userTask id="mgr" name="经理审批" flowable:candidateStrategy="30" flowable:candidateParam="1"/>
    <sequenceFlow id="f2" sourceRef="mgr" targetRef="end"/>
    <endEvent id="end" name="结束"/>
  </process>
</definitions>"""


def test_expense_workflow():
    # 部署（或更新）biz_expense 流程模型
    r = jpost("/bpm/model/create", dict(key="biz_expense", name="报销审批流程", bpmnXml=EXPENSE_BPMN,
              type=10, formType=20, visible=True, description="业务报销：审批通过自动改状态",
              formCustomCreatePath="/portal/expense", formCustomViewPath="/portal/expense",
              managerUserIds=[1]))
    body = r.json()
    exists = "已经存在" in body.get("msg", "")
    check("报销流程-模型创建(或已存在)", body.get("code") == 0 or exists, r.text[:150])
    r = jget("/bpm/model/list", params={"name": "报销审批流程"})
    rows = [m for m in r.json().get("data", []) if m["key"] == "biz_expense"]
    mid = rows[0]["id"] if rows else None
    if exists and mid:
        jput("/bpm/model/update", dict(id=mid, key="biz_expense", name="报销审批流程",
             bpmnXml=EXPENSE_BPMN, type=10, formType=20, visible=True,
             description="业务报销：审批通过自动改状态",
             formCustomCreatePath="/portal/expense", formCustomViewPath="/portal/expense",
             managerUserIds=[1]))
    if not exists:
        r = jpost("/bpm/model/deploy", params={"id": mid})
        check("报销流程-模型部署", r.json().get("code") == 0, r.text[:150])

    # 提交报销 -> 自动发起流程
    reason_tag = "报销流程测试%d" % (int(time.time()) % 1000000)
    r = jpost("/portal/expense-submit", dict(category="1", amount=66.6,
              expenseDate="2026-09-06", reason=reason_tag))
    check("报销流程-提交", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/expense-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if x.get("reason") == reason_tag]
    check("报销流程-流程实例已关联", rows and rows[0].get("processInstanceId"), str(rows[:1]))
    eid = rows[0]["id"] if rows else None
    proc_id = rows[0].get("processInstanceId") if rows else None

    # BPM 待办 -> 审批通过 -> 状态回调
    r = jget("/bpm/task/todo-page", params={"pageNo": 1, "pageSize": 20})
    tasks = [t for t in r.json().get("data", {}).get("list", []) if t.get("processInstanceId") == proc_id]
    check("报销流程-待办任务", len(tasks) == 1, str(tasks[:1]))
    if tasks:
        r = jput("/bpm/task/approve", dict(id=tasks[0]["id"], reason="同意（流程测试）"))
        check("报销流程-审批通过", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/expense-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if x.get("reason") == reason_tag]
    check("报销流程-状态回调已通过(1)", rows and rows[0].get("status") == "1", str(rows[:1]))
    if eid:
        jdelete("/portal/expense-withdraw", params={"id": eid})


# ---------------- 18. 补卡接入 Flowable（biz_correction 流程模型 -> 发起 -> 审批 -> 回写考勤） ----------------
CORRECTION_BPMN = """<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"   xmlns:flowable="http://flowable.org/bpmn"   targetNamespace="http://flowable.org/bpmn"   typeLanguage="http://www.w3.org/2001/XMLSchema"   expressionLanguage="http://www.w3.org/1999/XPath">
  <process id="biz_correction" name="补卡审批流程" isExecutable="true">
    <startEvent id="start" name="发起"/>
    <sequenceFlow id="f1" sourceRef="start" targetRef="mgr"/>
    <userTask id="mgr" name="经理审批" flowable:candidateStrategy="30" flowable:candidateParam="1"/>
    <sequenceFlow id="f2" sourceRef="mgr" targetRef="end"/>
    <endEvent id="end" name="结束"/>
  </process>
</definitions>"""


def test_correction_workflow():
    FDATE = (__import__("datetime").date.today() + __import__("datetime").timedelta(days=30)).strftime("%Y-%m-%d")
    # 部署（或更新）biz_correction 流程模型
    r = jpost("/bpm/model/create", dict(key="biz_correction", name="补卡审批流程", bpmnXml=CORRECTION_BPMN,
              type=10, formType=20, visible=True, description="业务补卡：审批通过自动回写考勤",
              formCustomCreatePath="/portal/correction", formCustomViewPath="/portal/correction",
              managerUserIds=[1]))
    body = r.json()
    exists = "已经存在" in body.get("msg", "")
    check("补卡流程-模型创建(或已存在)", body.get("code") == 0 or exists, r.text[:150])
    r = jget("/bpm/model/list", params={"name": "补卡审批流程"})
    rows = [m for m in r.json().get("data", []) if m["key"] == "biz_correction"]
    mid = rows[0]["id"] if rows else None
    if exists and mid:
        jput("/bpm/model/update", dict(id=mid, key="biz_correction", name="补卡审批流程",
             bpmnXml=CORRECTION_BPMN, type=10, formType=20, visible=True,
             description="业务补卡：审批通过自动回写考勤",
             formCustomCreatePath="/portal/correction", formCustomViewPath="/portal/correction",
             managerUserIds=[1]))
    if not exists:
        r = jpost("/bpm/model/deploy", params={"id": mid})
        check("补卡流程-模型部署", r.json().get("code") == 0, r.text[:150])

    # 清残留
    r = jget("/portal/correction-page", params={"pageNo": 1, "pageSize": 20})
    for row in r.json().get("data", {}).get("list", []):
        if "补卡流程测试" in (row.get("reason") or ""):
            jdelete("/portal/correction-withdraw", params={"id": row["id"]})

    # 提交补卡 -> 自动发起流程
    r = jpost("/portal/correction-submit", dict(workDate=FDATE, correctType="1",
              correctTime="08:50", reason="补卡流程测试：外出办事"))
    check("补卡流程-提交", r.json().get("code") == 0, r.text[:150])
    r = jget("/portal/correction-page", params={"pageNo": 1, "pageSize": 20})
    rows = [x for x in r.json().get("data", {}).get("list", []) if "补卡流程测试" in (x.get("reason") or "")]
    check("补卡流程-流程实例已关联", rows and rows[0].get("processInstanceId"), str(rows[:1]))
    cid = rows[0]["id"] if rows else None
    proc_id = rows[0].get("processInstanceId") if rows else None

    # 待办审批 -> 自动回写考勤
    r = jget("/bpm/task/todo-page", params={"pageNo": 1, "pageSize": 20})
    tasks = [t for t in r.json().get("data", {}).get("list", []) if t.get("processInstanceId") == proc_id]
    check("补卡流程-待办任务", len(tasks) == 1, str(tasks[:1]))
    if tasks:
        r = jput("/bpm/task/approve", dict(id=tasks[0]["id"], reason="同意（流程测试）"))
        check("补卡流程-审批通过", r.json().get("code") == 0, r.text[:150])
    r = jget("/biz/attendance/page", params={"empName": "管理员", "workDate": FDATE, "pageNo": 1, "pageSize": 5})
    rows = r.json().get("data", {}).get("list", [])
    check("补卡流程-考勤自动回写", rows and rows[0].get("checkIn") == "08:50" and rows[0].get("status") == "0",
          str(rows[:1]))

    # 清理
    if cid:
        jdelete("/biz/correction/delete", params={"id": cid})
    r = jget("/biz/attendance/page", params={"empName": "管理员", "workDate": FDATE, "pageNo": 1, "pageSize": 5})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/attendance/delete", params={"id": row["id"]})


def seed_demo_data():
    """为前端验收准备演示数据（每类 2~3 条，幂等可重复）"""
    H = {"Authorization": "Bearer " + TOKEN["value"], "tenant-id": "1"}
    def mk(path, body, uniq):
        r = jpost("/biz/%s/create" % path, body)
        msg = r.json().get("msg", "")
        return "已经存在" not in msg
    # 客户
    phones = {"杭州云启科技": "13800001111", "上海恒达贸易": "13800002222", "北京科创能源": "13800003333"}
    for name, contact, ind in [("杭州云启科技", "张经理", "互联网"), ("上海恒达贸易", "李总", "贸易"), ("北京科创能源", "王工", "能源")]:
        mk("customer", dict(customerName=name, contactPerson=contact, phone=phones[name], industry=ind, status="0"), name)
    # 产品
    for code, name, cat, price in [("P-001", "智能网关 G100", "网络设备", "1299.00"), ("P-002", "无线AP W50", "网络设备", "599.00"), ("P-003", "工控机 I7", "工控设备", "4999.00")]:
        mk("product", dict(productCode=code, productName=name, category=cat, unit="台", price=price, status="0"), name)
    # 供应商
    for name in ("深圳快联电子", "杭州网域科技"):
        mk("supplier", dict(supplierName=name, contactPerson="刘工", status="0"), name)
    # 采购单（草稿2张+已完成1张）
    for i, (code, sup, prod, qty, st) in enumerate([
            ("CG-D001", "深圳快联电子", "智能网关 G100", 20, "0"),
            ("CG-D002", "杭州网域科技", "无线AP W50", 30, "0"),
            ("CG-C001", "深圳快联电子", "工控机 I7", 5, "2")]):
        mk("purchase", dict(purchaseCode=code, supplierName=sup, productName=prod,
                            quantity=qty, price=100, totalAmount=qty*100,
                            purchaseDate="2026-09-0%d" % (i+1), status=st), code)
    # 销售单（已完成 2 张）
    for code, cust, prod, qty in [("XS-C001", "杭州云启科技", "智能网关 G100", 3),
                                   ("XS-C002", "上海恒达贸易", "无线AP W50", 8)]:
        mk("sales", dict(salesCode=code, customerName=cust, productName=prod,
                         quantity=qty, price=150, totalAmount=qty*150,
                         salesDate="2026-09-06", status="2"), code)
    # 员工
    for no, name, dept, post in [("E001", "张伟", "研发部", "后端工程师"), ("E002", "李娜", "研发部", "前端工程师"), ("E003", "王强", "销售部", "客户经理")]:
        mk("employee", dict(empNo=no, empName=name, deptName=dept, postName=post, status="0"), no)
    # 考勤（今天 + 昨天）
    from datetime import date, timedelta
    today = date.today().strftime("%Y-%m-%d")
    yest = (date.today() - timedelta(days=1)).strftime("%Y-%m-%d")
    mk("attendance", dict(empName="张伟", workDate=today, checkIn="08:55", checkOut="18:10", status="0"), "tw")
    mk("attendance", dict(empName="李娜", workDate=today, checkIn="09:12", checkOut="18:30", status="1"), "tl")
    mk("attendance", dict(empName="张伟", workDate=yest, checkIn="08:50", checkOut="19:30", status="0"), "yw")
    # 假期余额 + 请假（草稿，待审批）
    mk("quota", dict(empName="张伟", leaveType="3", year="2026", quotaDays=5), "q-张伟-3-2026")
    mk("quota", dict(empName="李娜", leaveType="3", year="2026", quotaDays=5), "q-李娜-3-2026")
    r = jpost("/portal/leave-submit", dict(leaveType="3", startDate="2026-09-28",
              endDate="2026-09-29", days=2, reason="家中有事（演示待审批）"), headers=H)
    # 合同
    mk("contract", dict(contractCode="HT-2026-001", customerName="杭州云启科技", productName="智能网关 G100",
                        amount=25980, signDate="2026-08-15", owner="张伟", status="1"), "HT-2026-001")
    # 费用报销（待审批 1 + 已通过 1）
    r = jpost("/portal/expense-submit", dict(category="1", amount=880.00, expenseDate="2026-09-05",
              reason="出差杭州交通住宿（演示）"), headers=H)
    # 补卡（待审批）
    r = jpost("/portal/correction-submit", dict(workDate="2026-09-04", correctType="1",
              correctTime="08:50", reason="忘打卡（演示）"), headers=H)
    # 业务汇报
    r = jpost("/portal/report-submit", dict(reportType="1", reportDate="2026-09-06",
              title="本周研发进展汇报", content="完成企业管理系统迁移与功能补全，BPM 工作流引擎上线。"), headers=H)
    print("[seed] 演示数据就绪")


# ---------------- 13. 收付款管理（对齐 yudao ERP 收付款单 / CRM 回款） ----------------
def test_payment():
    import datetime
    today = datetime.date.today().strftime("%Y-%m-%d")
    # 清残留：删历史测试收付款与单据
    r = jget("/biz/payment/page", params={"orderCode": "PAY", "pageNo": 1, "pageSize": 50})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/payment/delete", params={"id": row["id"]})
    for mod, field, val in [("sales", "salesCode", "XSPAY01"), ("purchase", "purchaseCode", "CGPAY01")]:
        rid = find_id(mod, field, val)
        if rid:
            jdelete("/biz/%s/delete" % mod, params={"id": rid})
    r = jget("/biz/product/page", params={"productCode": "R-PAY", "pageNo": 1, "pageSize": 5})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/product/delete", params={"id": row["id"]})

    # 建产品 + 采购单(50x4=200，完成后入库) + 销售单(30x10=300，完成后出库)
    jpost("/biz/product/create", dict(productCode="R-PAY", productName=TAG + "P", category="测试",
                                      unit="个", price=4, status="0"))
    r = jpost("/biz/purchase/create", dict(purchaseCode="CGPAY01", supplierName="收付款供应商",
              productName=TAG + "P", quantity=50, price=4, purchaseDate=today))
    check("收付款-采购单创建", r.json().get("code") == 0, r.text[:120])
    pid = find_id("purchase", "purchaseCode", "CGPAY01")
    jpost("/biz/purchase/transition", params={"id": pid, "action": "confirm"})
    r = jpost("/biz/purchase/complete", params={"id": pid})
    check("收付款-采购单完成", r.json().get("code") == 0, r.text[:120])
    r = jpost("/biz/sales/create", dict(salesCode="XSPAY01", customerName="收付款客户",
              productName=TAG + "P", quantity=30, price=10, salesDate=today))
    check("收付款-销售单创建", r.json().get("code") == 0, r.text[:120])
    sid = find_id("sales", "salesCode", "XSPAY01")
    jpost("/biz/sales/transition", params={"id": sid, "action": "confirm"})
    r = jpost("/biz/sales/complete", params={"id": sid})
    check("收付款-销售单完成", r.json().get("code") == 0, r.text[:120])

    # 销售收款 200
    r = jpost("/biz/payment/create", dict(paymentType="1", bizType="1", orderId=sid,
              amount=200, paymentMethod="2", paymentDate=today, reason=None, remark="测试收款"))
    check("收付款-销售收款登记", r.json().get("code") == 0, r.text[:150])
    # 超总额拒绝：300 已收 200 再收 150
    r = jpost("/biz/payment/create", dict(paymentType="1", bizType="1", orderId=sid,
              amount=150, paymentMethod="1", paymentDate=today, remark="超收测试"))
    check("收付款-累计超总额被拒", r.json().get("code") != 0 and "超出" in r.json().get("msg", ""), r.text[:150])
    r = jget("/biz/payment/paid-sum", params={"bizType": "1", "orderId": sid})
    check("收付款-已收金额汇总", r.json().get("code") == 0 and float(r.json().get("data", 0)) == 200.0,
          r.text[:120])
    # 未完成单据拒绝
    r = jpost("/biz/purchase/create", dict(purchaseCode="CGPAY02", supplierName="收付款供应商",
              productName=TAG + "P", quantity=1, price=4, purchaseDate=today))
    pid_draft = find_id("purchase", "purchaseCode", "CGPAY02")
    r = jpost("/biz/payment/create", dict(paymentType="2", bizType="2", orderId=pid_draft,
              amount=1, paymentMethod="1", paymentDate=today, remark="草稿付款测试"))
    check("收付款-草稿单登记被拒", r.json().get("code") != 0, r.text[:150])
    # 类型不匹配拒绝：收款关联采购单
    r = jpost("/biz/payment/create", dict(paymentType="1", bizType="2", orderId=pid,
              amount=1, paymentMethod="1", paymentDate=today, remark="类型不匹配测试"))
    check("收付款-收付类型不匹配被拒", r.json().get("code") != 0, r.text[:150])
    # 采购付款 100
    r = jpost("/biz/payment/create", dict(paymentType="2", bizType="2", orderId=pid,
              amount=100, paymentMethod="3", paymentDate=today, remark="测试付款"))
    check("收付款-采购付款登记", r.json().get("code") == 0, r.text[:150])
    # 分页可见两条
    r = jget("/biz/payment/page", params={"orderCode": "PAY", "pageNo": 1, "pageSize": 10})
    rows = r.json().get("data", {}).get("list", [])
    check("收付款-分页查询", r.json().get("code") == 0 and len(rows) == 2, "rows=%d" % len(rows))
    # 看板本月收款统计
    r = jpost("/biz/dashboard/panel")
    check("收付款-看板本月收款统计", r.json().get("code") == 0
          and float(r.json().get("data", {}).get("monthReceived", 0)) >= 200.0, r.text[:150])
    # 删除流水后汇总归零
    for row in rows:
        if row.get("remark") == "测试收款":
            jdelete("/biz/payment/delete", params={"id": row["id"]})
    r = jget("/biz/payment/paid-sum", params={"bizType": "1", "orderId": sid})
    check("收付款-删除后汇总归零", r.json().get("code") == 0 and float(r.json().get("data", 0)) == 0.0,
          r.text[:120])
    # 清理单据与产品
    for mod, field, val in [("sales", "salesCode", "XSPAY01"), ("purchase", "purchaseCode", "CGPAY01"),
                            ("purchase", "purchaseCode", "CGPAY02")]:
        rid = find_id(mod, field, val)
        if rid:
            jdelete("/biz/%s/delete" % mod, params={"id": rid})
    pid2 = find_id("product", "productCode", "R-PAY")
    if pid2:
        jdelete("/biz/product/delete", params={"id": pid2})
    # 剩余流水也清理，保证下月看板统计不受影响
    r = jget("/biz/payment/page", params={"orderCode": "PAY", "pageNo": 1, "pageSize": 50})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/payment/delete", params={"id": row["id"]})



# ---------------- 14. 库存盘点（对齐 yudao ERP StockCheck） ----------------
def test_stockcheck():
    import datetime
    today = datetime.date.today().strftime("%Y-%m-%d")
    # 清残留：历史测试盘点单与单据
    r = jget("/biz/stockcheck/page", params={"productName": TAG + "P", "pageNo": 1, "pageSize": 50})
    for row in r.json().get("data", {}).get("list", []):
        if row.get("status") == "0":
            jdelete("/biz/stockcheck/delete", params={"id": row["id"]})
    for mod, field, val in [("sales", "salesCode", "XSPAY01"), ("purchase", "purchaseCode", "CGPAY01"),
                            ("purchase", "purchaseCode", "CGPAY02"), ("purchase", "purchaseCode", "CGCK01")]:
        rid = find_id(mod, field, val)
        if rid:
            jdelete("/biz/%s/delete" % mod, params={"id": rid})
    r = jget("/biz/product/page", params={"productCode": "R-PAY", "pageNo": 1, "pageSize": 5})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/product/delete", params={"id": row["id"]})

    # 建产品 + 采购完成（库存 50）
    jpost("/biz/product/create", dict(productCode="R-PAY", productName=TAG + "P", category="测试",
                                      unit="个", price=4, status="0"))
    jpost("/biz/purchase/create", dict(purchaseCode="CGCK01", supplierName="盘点供应商",
              productName=TAG + "P", quantity=50, price=4, purchaseDate=today))
    pid = find_id("purchase", "purchaseCode", "CGCK01")
    jpost("/biz/purchase/transition", params={"id": pid, "action": "confirm"})
    r = jpost("/biz/purchase/complete", params={"id": pid})
    check("盘点-采购完成入库", r.json().get("code") == 0, r.text[:120])
    base_stock = get_stock(TAG + "P")  # 动态基准（payment 测试可能已占用同一产品库存行）

    # 创建盘点单：实盘 47（快照当前账面）
    r = jpost("/biz/stockcheck/create", dict(productName=TAG + "P", actualQuantity=47,
              checkDate=today, remark="测试盘点：少 3 个"))
    check("盘点-创建成功", r.json().get("code") == 0, r.text[:150])
    rows = jget("/biz/stockcheck/page", params={"productName": TAG + "P", "pageNo": 1, "pageSize": 5}).json().get("data", {}).get("list", [])
    rows = [x for x in rows if x.get("remark") == "测试盘点：少 3 个"]
    cid = rows[0]["id"] if rows else None
    check("盘点-快照账面=当下库存", rows and rows[0].get("bookQuantity") == base_stock
          and rows[0].get("status") == "0", str(rows[:1]))

    # 创建与确认之间库存变动：再入库 10（库存 60），确认时按当下账面重算
    jpost("/biz/purchase/create", dict(purchaseCode="CGCK02", supplierName="盘点供应商",
              productName=TAG + "P", quantity=10, price=4, purchaseDate=today))
    pid2 = find_id("purchase", "purchaseCode", "CGCK02")
    jpost("/biz/purchase/transition", params={"id": pid2, "action": "confirm"})
    jpost("/biz/purchase/complete", params={"id": pid2})
    check("盘点-入库后库存+10", get_stock(TAG + "P") == base_stock + 10, "stock=%s" % get_stock(TAG + "P"))

    # 确认盘点：账面 = 当下库存 → 实盘 47，库存调整为 47
    r = jpost("/biz/stockcheck/confirm", params={"id": cid})
    check("盘点-确认成功", r.json().get("code") == 0, r.text[:150])
    check("盘点-库存按实盘调整为47", get_stock(TAG + "P") == 47, "stock=%s" % get_stock(TAG + "P"))
    rows = jget("/biz/stockcheck/page", params={"productName": TAG + "P", "pageNo": 1, "pageSize": 5}).json().get("data", {}).get("list", [])
    row = [x for x in rows if x.get("id") == cid][0]
    check("盘点-差异已确认", row.get("diffQuantity") == 47 - (base_stock + 10)
          and row.get("bookQuantity") == base_stock + 10
          and row.get("status") == "1", str(row))
    r = jget("/biz/stockmove/page", params={"productName": TAG + "P", "pageNo": 1, "pageSize": 5})
    moves = r.json().get("data", {}).get("list", [])
    check("盘点-流水溯源", moves and moves[0].get("sourceType") == "stockcheck"
          and moves[0].get("moveType") == "2", str(moves[:1]))

    # 重复确认被拒；已确认删除被拒
    r = jpost("/biz/stockcheck/confirm", params={"id": cid})
    check("盘点-重复确认被拒", r.json().get("code") != 0, r.text[:120])
    r = jdelete("/biz/stockcheck/delete", params={"id": cid})
    check("盘点-已确认不可删除", r.json().get("code") != 0, r.text[:120])

    # 待确认可删除
    r = jpost("/biz/stockcheck/create", dict(productName=TAG + "P", actualQuantity=100,
              checkDate=today, remark="盘点测试：待删除"))
    rows = jget("/biz/stockcheck/page", params={"productName": TAG + "P", "pageNo": 1, "pageSize": 5}).json().get("data", {}).get("list", [])
    rows = [x for x in rows if x.get("remark") == "盘点测试：待删除"]
    cid2 = rows[0]["id"] if rows else None
    r = jdelete("/biz/stockcheck/delete", params={"id": cid2})
    check("盘点-待确认可删除", r.json().get("code") == 0, r.text[:120])

    # 清理单据与产品（已确认盘点单保留作为库存轨迹）
    for mod, field, val in [("purchase", "purchaseCode", "CGCK01"), ("purchase", "purchaseCode", "CGCK02")]:
        rid = find_id(mod, field, val)
        if rid:
            jdelete("/biz/%s/delete" % mod, params={"id": rid})
    pid3 = find_id("product", "productCode", "R-PAY")
    if pid3:
        jdelete("/biz/product/delete", params={"id": pid3})



# ---------------- 15. 销售/采购退货（对齐 yudao ERP ErpSaleReturn / ErpPurchaseReturn） ----------------
def test_return():
    import datetime
    today = datetime.date.today().strftime("%Y-%m-%d")
    # 清残留：删历史测试退货单与单据
    r = jget("/biz/return/page", params={"orderCode": "RET", "pageNo": 1, "pageSize": 50})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/return/delete", params={"id": row["id"]})
    for mod, field, val in [("sales", "salesCode", "XSRET01"), ("purchase", "purchaseCode", "CGRET01"),
                            ("purchase", "purchaseCode", "CGRET02")]:
        rid = find_id(mod, field, val)
        if rid:
            jdelete("/biz/%s/delete" % mod, params={"id": rid})
    r = jget("/biz/product/page", params={"productCode": "R-RET", "pageNo": 1, "pageSize": 5})
    for row in r.json().get("data", {}).get("list", []):
        jdelete("/biz/product/delete", params={"id": row["id"]})

    # 建产品 + 采购单(50x4=200 完成入库) + 销售单(30x10=300 完成出库)
    jpost("/biz/product/create", dict(productCode="R-RET", productName=TAG + "R", category="测试",
                                      unit="个", price=4, status="0"))
    r = jpost("/biz/purchase/create", dict(purchaseCode="CGRET01", supplierName="退货供应商",
              productName=TAG + "R", quantity=50, price=4, purchaseDate=today))
    check("退货-采购单创建", r.json().get("code") == 0, r.text[:120])
    pid = find_id("purchase", "purchaseCode", "CGRET01")
    jpost("/biz/purchase/transition", params={"id": pid, "action": "confirm"})
    r = jpost("/biz/purchase/complete", params={"id": pid})
    check("退货-采购单完成入库", r.json().get("code") == 0, r.text[:120])
    r = jpost("/biz/sales/create", dict(salesCode="XSRET01", customerName="退货客户",
              productName=TAG + "R", quantity=30, price=10, salesDate=today))
    check("退货-销售单创建", r.json().get("code") == 0, r.text[:120])
    sid = find_id("sales", "salesCode", "XSRET01")
    jpost("/biz/sales/transition", params={"id": sid, "action": "confirm"})
    r = jpost("/biz/sales/complete", params={"id": sid})
    check("退货-销售单完成出库", r.json().get("code") == 0, r.text[:120])

    # --- 销售退货：退 5 件入库 ---
    base = get_stock(TAG + "R")
    r = jpost("/biz/return/create", dict(returnType="1", orderId=sid, quantity=5,
              returnDate=today, reason="质量问题", remark="测试销售退货"))
    check("退货-销售退货创建", r.json().get("code") == 0, r.text[:150])
    rid1 = r.json().get("data")
    r = jget("/biz/return/returned-sum", params={"returnType": "1", "orderId": sid})
    check("退货-已退数量汇总", r.json().get("code") == 0 and int(r.json().get("data", 0)) == 5, r.text[:120])
    # 累计超原单拒绝：30 已退 5 再退 26
    r = jpost("/biz/return/create", dict(returnType="1", orderId=sid, quantity=26,
              returnDate=today, reason="超量测试"))
    check("退货-累计超原单被拒", r.json().get("code") != 0 and "超出" in r.json().get("msg", ""), r.text[:150])
    # 未完成单据拒绝：草稿采购单 CGRET02
    r = jpost("/biz/purchase/create", dict(purchaseCode="CGRET02", supplierName="退货供应商",
              productName=TAG + "R", quantity=10, price=4, purchaseDate=today))
    pid_draft = find_id("purchase", "purchaseCode", "CGRET02")
    r = jpost("/biz/return/create", dict(returnType="2", orderId=pid_draft, quantity=1,
              returnDate=today, reason="草稿测试"))
    check("退货-草稿单退货被拒", r.json().get("code") != 0, r.text[:150])
    # 类型不匹配拒绝：销售退货关联采购单（按销售单查不到该 id）
    r = jpost("/biz/return/create", dict(returnType="1", orderId=pid, quantity=1,
              returnDate=today, reason="类型不匹配测试"))
    check("退货-类型与单据不匹配被拒", r.json().get("code") != 0, r.text[:150])
    # 执行销售退货：入库 +5
    r = jput("/biz/return/execute", params={"id": rid1})
    check("退货-销售退货执行入库", r.json().get("code") == 0, r.text[:150])
    check("退货-入库后库存+5", get_stock(TAG + "R") == base + 5,
          "base=%s now=%s" % (base, get_stock(TAG + "R")))
    # 重复执行被拒
    r = jput("/biz/return/execute", params={"id": rid1})
    check("退货-重复执行被拒", r.json().get("code") != 0, r.text[:150])
    # 编辑已退货单被拒
    r = jput("/biz/return/update", dict(id=rid1, returnType="1", orderId=sid, quantity=6,
              returnDate=today, reason="改量测试"))
    check("退货-已退货单编辑被拒", r.json().get("code") != 0, r.text[:150])

    # --- 采购退货：退 10 件出库 ---
    base2 = get_stock(TAG + "R")
    r = jpost("/biz/return/create", dict(returnType="2", orderId=pid, quantity=10,
              returnDate=today, reason="规格不符", remark="测试采购退货"))
    check("退货-采购退货创建", r.json().get("code") == 0, r.text[:150])
    rid2 = r.json().get("data")
    r = jput("/biz/return/execute", params={"id": rid2})
    check("退货-采购退货执行出库", r.json().get("code") == 0, r.text[:150])
    check("退货-出库后库存-10", get_stock(TAG + "R") == base2 - 10,
          "base=%s now=%s" % (base2, get_stock(TAG + "R")))

    # --- 作废：新建销售退货 3 件再作废，库存不变、汇总不计入 ---
    r = jpost("/biz/return/create", dict(returnType="1", orderId=sid, quantity=3,
              returnDate=today, reason="作废测试"))
    rid3 = r.json().get("data")
    r = jput("/biz/return/void", params={"id": rid3})
    check("退货-作废成功", r.json().get("code") == 0, r.text[:150])
    check("退货-作废后库存不变", get_stock(TAG + "R") == base2 - 10, "库存漂移")
    r = jget("/biz/return/returned-sum", params={"returnType": "1", "orderId": sid})
    check("退货-作废不计入汇总", r.json().get("code") == 0 and int(r.json().get("data", 0)) == 5, r.text[:120])

    # 分页可见 3 张
    r = jget("/biz/return/page", params={"orderCode": "RET", "pageNo": 1, "pageSize": 10})
    rows = r.json().get("data", {}).get("list", [])
    check("退货-分页查询", r.json().get("code") == 0 and len(rows) == 3, "rows=%d" % len(rows))

    # 清理退货单（已退货删除不回补库存，属管理员兜底操作）与单据、产品
    for row in rows:
        jdelete("/biz/return/delete", params={"id": row["id"]})
    r = jget("/biz/return/page", params={"orderCode": "RET", "pageNo": 1, "pageSize": 10})
    check("退货-清理完成", len(r.json().get("data", {}).get("list", [])) == 0, "残留退货单")
    for mod, field, val in [("sales", "salesCode", "XSRET01"), ("purchase", "purchaseCode", "CGRET01"),
                            ("purchase", "purchaseCode", "CGRET02")]:
        rid = find_id(mod, field, val)
        if rid:
            jdelete("/biz/%s/delete" % mod, params={"id": rid})
    rid4 = find_id("product", "productCode", "R-RET")
    if rid4:
        jdelete("/biz/product/delete", params={"id": rid4})


# ---------------- 16. 注册审批联动（部门/职位 + 管理员审批 + 预留部门角色映射） ----------------
def test_register_flow():
    import datetime
    today = datetime.date.today().strftime("%Y-%m-%d")
    stamp = str(int(time.time()) % 100000)
    uname = "regflow" + stamp[-4:]
    # 取启用部门/岗位选项（未登录接口）
    r = req("get", "/system/auth/register-options")
    opts = r.json().get("data", {})
    check("注册-下拉选项接口", r.json().get("code") == 0 and len(opts.get("depts", [])) > 0, r.text[:120])
    dept_id = opts["depts"][0]["id"]
    post_id = (opts.get("posts") or [{}])[0].get("id")

    # 1) 注册（提交申请）
    body = dict(username=uname, nickname="流程注册测试", password="Test123456", deptId=dept_id)
    if post_id:
        body["postId"] = post_id
    r = jpost("/system/auth/register", body)
    check("注册-提交申请", r.json().get("code") == 0 and r.json().get("data") is True, r.text[:150])

    # 2) 待审批：登录被明确提示
    r = jpost("/system/auth/login", dict(username=uname, password="Test123456"))
    check("注册-待审批登录被拒且提示明确", r.json().get("code") != 0
          and "等待管理员审批" in r.json().get("msg", ""), r.text[:150])

    # 3) 管理员可见待审批列表
    r = jget("/system/register-apply/page", params={"username": uname, "status": "0", "pageNo": 1, "pageSize": 5})
    rows = r.json().get("data", {}).get("list", [])
    check("注册-待审批列表可见", r.json().get("code") == 0 and len(rows) == 1, r.text[:120])
    aid = rows[0]["id"] if rows else None

    # 4) 审批通过：账号创建、入部门、绑岗位、分配普通角色
    r = jpost("/system/register-apply/approve", params={"id": aid})
    check("注册-审批通过", r.json().get("code") == 0, r.text[:120])
    uid = find_id("system/users-page?", "username", uname) if False else None
    r = jget("/system/user/page", params={"pageNo": 1, "pageSize": 5, "username": uname})
    urows = r.json().get("data", {}).get("list", [])
    check("注册-账号已创建", urows and urows[0].get("status") == 0 and urows[0].get("deptId") == dept_id,
          str(urows[:1]))
    uid = urows[0]["id"] if urows else None
    r = jget("/system/permission/list-user-roles", params={"userId": uid})
    roles = r.json().get("data", [])
    check("注册-已分配普通角色", r.json().get("code") == 0 and len(roles) >= 1, r.text[:120])

    # 5) 审批后可登录
    r = jpost("/system/auth/login", dict(username=uname, password="Test123456"))
    check("注册-审批后可登录", r.json().get("code") == 0, r.text[:120])

    # 6) 重复注册同名被拒
    r = jpost("/system/auth/register", body)
    check("注册-重复用户名被拒", r.json().get("code") != 0, r.text[:120])

    # 7) 驳回分支：注册第二个用户并驳回
    uname2 = "regrej" + stamp[-4:]
    body2 = dict(username=uname2, nickname="驳回测试", password="Test123456", deptId=dept_id)
    r = jpost("/system/auth/register", body2)
    check("注册-第二笔提交", r.json().get("code") == 0, r.text[:120])
    r = jget("/system/register-apply/page", params={"username": uname2, "status": "0", "pageNo": 1, "pageSize": 5})
    rows2 = r.json().get("data", {}).get("list", [])
    aid2 = rows2[0]["id"] if rows2 else None
    r = jpost("/system/register-apply/reject", params={"id": aid2, "reason": "信息不符"})
    check("注册-驳回成功", r.json().get("code") == 0, r.text[:120])
    r = jpost("/system/auth/login", dict(username=uname2, password="Test123456"))
    check("注册-驳回后登录被拒", r.json().get("code") != 0, r.text[:120])

    # 8) 预留接口：部门默认角色映射
    r = jpost("/system/dept-role-map/create", params={"deptId": dept_id, "roleId": roles[0] if roles else 2})
    check("映射-创建", r.json().get("code") == 0, r.text[:120])
    r = jget("/system/dept-role-map/list-by-dept", params={"deptId": dept_id})
    maps = r.json().get("data", [])
    check("映射-按部门查询", r.json().get("code") == 0 and len(maps) >= 1, r.text[:120])
    mid = maps[0]["id"] if maps else None
    if mid:
        r = jdelete("/system/dept-role-map/delete", params={"id": mid})
        check("映射-删除", r.json().get("code") == 0, r.text[:120])

    # 清理：删除测试账号
    if uid:
        jdelete("/system/user/delete", params={"id": uid})



if __name__ == "__main__":
    test_auth()
    seed_demo_data()
    test_crud()
    test_rules()
    test_dashboard()
    test_portal()
    test_quota()
    test_expense()
    test_correction()
    test_overtime()
    test_followup()
    test_expense_workflow()
    test_correction_workflow()
    test_digest()
    test_file_upload()
    test_bpm()
    test_leave_workflow()
    test_system()
    test_payment()
    test_stockcheck()
    test_return()
    test_register_flow()
    failed = [x for x in results if not x[1]]
    print("\n========== 测试汇总 ==========")
    print("总计: %d  通过: %d  失败: %d" % (len(results), len(results) - len(failed), len(failed)))
    for name, ok, detail in failed:
        print("  [FAIL] %s  %s" % (name, detail))
    import sys
    sys.exit(1 if failed else 0)
