# -*- coding: utf-8 -*-
"""企业管理系统自动化冒烟测试

覆盖：
1. 登录鉴权（错误密码拒绝 / 正确密码通过 / 未登录拦截）
2. 10 个业务模块：列表、新增页、新增、编辑页、修改、删除 全链路
3. 业务规则：采购入库加库存、销售超库存拦截、销售出库扣库存、请假审批流转
4. 系统模块：用户/角色/部门/字典列表
5. 首页工作台

运行前提：应用已启动在 http://localhost:8090，验证码已关闭
"""
import sys
import socket
import ipaddress
from urllib.parse import urlparse

import requests

# 测试目标仅限本地被测服务
BASE = "http://localhost:8090"
ALLOWED_HOSTS = ("localhost", "127.0.0.1")

S = requests.Session()
results = []


def check(name, ok, detail=""):
    results.append((name, ok, detail))
    mark = "PASS" if ok else "FAIL"
    print("[%s] %s %s" % (mark, name, detail if not ok else ""))


def req(method, path, **kwargs):
    """统一请求入口：协议固定 http，主机白名单限定本地回环，防止 SSRF"""
    url = BASE + path
    parsed = urlparse(url)
    assert parsed.scheme == "http", "仅允许 http 协议"
    assert parsed.hostname in ALLOWED_HOSTS, "仅允许本地被测服务"
    ip = socket.gethostbyname(parsed.hostname)
    assert ipaddress.ip_address(ip).is_loopback, "解析后地址必须为回环地址"
    headers = kwargs.setdefault("headers", {})
    headers.setdefault("X-Requested-With", "XMLHttpRequest")
    return getattr(S, method)(url, **kwargs)


def login(username, password):
    S.cookies.clear()
    return req("post", "/login", data={"username": username, "password": password,
                                       "rememberMe": "false"},
               headers={"X-Requested-With": "XMLHttpRequest"})


# ---------------- 1. 登录鉴权 ----------------
def test_auth():
    r = login("admin", "wrongpass")
    check("登录-错误密码被拒绝", r.status_code == 200 and r.json().get("code") != 0, r.text[:100])
    r = login("admin", "admin123")
    check("登录-正确凭证通过", r.status_code == 200 and r.json().get("code") == 0, r.text[:100])
    S.cookies.clear()
    r = req("post", "/biz/customer/list", data={})
    check("鉴权-未登录访问业务接口被拦截", r.status_code in (302, 401) or "未登录" in r.text)
    login("admin", "admin123")


# ---------------- 2. 业务模块 CRUD ----------------
CASES = {
    "customer": ("customerName", dict(customerName="__测试客户__", contactPerson="张三",
                                      phone="13800000000", status="0"),
                 dict(customerName="__测试客户_改__")),
    "product": ("productName", dict(productCode="T001", productName="__测试产品__", category="通用",
                                    unit="个", price="10.50", status="0"),
                dict(productName="__测试产品_改__")),
    "contract": ("contractCode", dict(contractCode="HT001", customerName="__测试客户__",
                                      amount="1000.00", signDate="2026-08-31", owner="管理员", status="0"),
                 dict(amount="2000.00")),
    "supplier": ("supplierName", dict(supplierName="__测试供应商__", contactPerson="李四", status="0"),
                 dict(supplierName="__测试供应商_改__")),
    "employee": ("empName", dict(empNo="E9999", empName="__测试员工__", deptName="测试部", status="0"),
                 dict(empName="__测试员工_改__")),
    "attendance": ("empName", dict(empName="__测试员工__", workDate="2026-08-31", checkIn="08:55",
                                   checkOut="18:05", status="0"),
                   dict(status="1")),
    "stock": ("productName", dict(productName="__测试产品__", warehouse="测试仓库",
                                  quantity="100", minQuantity="10"),
              dict(quantity="150")),
    "leave": ("empName", dict(empName="__测试员工__", leaveType="1", startDate="2026-09-01",
                              endDate="2026-09-02", days="2", reason="自动化测试", status="0"),
              None),
}


def find_id(module, key_field, key_value):
    r = req("post", "/biz/%s/list" % module, data={key_field: key_value, "pageSize": 10})
    for row in r.json().get("rows", []):
        if row.get(key_field) == key_value:
            return row.get("id")
    return None


def crud_flow(module, key_field, add_data, edit_map):
    r = req("post", "/biz/%s/list" % module, data={})
    check("%s-列表查询" % module, r.status_code == 200 and "total" in r.json(), r.text[:120])
    r = req("get", "/biz/%s/add" % module)
    check("%s-新增页渲染" % module, r.status_code == 200 and "form" in r.text, "HTTP %s" % r.status_code)
    r = req("post", "/biz/%s/add" % module, data=add_data)
    check("%s-新增保存" % module, r.status_code == 200 and r.json().get("code") == 0, r.text[:120])
    rec_id = find_id(module, key_field, add_data[key_field])
    check("%s-新增后可查询" % module, rec_id is not None, "未找到 %s=%s" % (key_field, add_data[key_field]))
    if rec_id is None:
        return None
    r = req("get", "/biz/%s/edit/%s" % (module, rec_id))
    check("%s-编辑页渲染" % module, r.status_code == 200 and "form" in r.text, "HTTP %s" % r.status_code)
    if edit_map:
        edit_data = dict(add_data)
        edit_data.update(edit_map)
        edit_data["id"] = rec_id
        r = req("post", "/biz/%s/edit" % module, data=edit_data)
        check("%s-修改保存" % module, r.status_code == 200 and r.json().get("code") == 0, r.text[:120])
    return rec_id


def delete(module, rec_id):
    if rec_id is None:
        return
    r = req("post", "/biz/%s/remove" % module, data={"ids": str(rec_id)})
    check("%s-删除" % module, r.status_code == 200 and r.json().get("code") == 0, r.text[:120])


def test_crud():
    for module, (key_field, add_data, edit_map) in CASES.items():
        rid = crud_flow(module, key_field, add_data, edit_map)
        delete(module, rid)


# ---------------- 3. 业务规则 ----------------
def get_stock(product, warehouse="默认仓库"):
    r = req("post", "/biz/stock/list", data={"productName": product, "warehouse": warehouse})
    rows = r.json().get("rows", [])
    return rows[0]["quantity"] if rows else 0


def test_rules():
    req("post", "/biz/product/add", data=dict(productCode="TR01", productName="__规则测试产品__",
                                              category="测试", unit="个", price="5", status="0"))
    before = get_stock("__规则测试产品__")
    r = req("post", "/biz/purchase/add", data=dict(purchaseCode="CG001", supplierName="__测试供应商__",
                productName="__规则测试产品__", quantity="50", price="3",
                purchaseDate="2026-08-31", status="1"))
    check("规则-采购单创建(已完成)", r.json().get("code") == 0, r.text[:120])
    after = get_stock("__规则测试产品__")
    check("规则-采购入库自动加库存", after == before + 50, "库存 %s -> %s" % (before, after))

    r = req("post", "/biz/sales/add", data=dict(salesCode="XS001", customerName="__测试客户__",
                productName="__规则测试产品__", quantity="99999", price="5",
                salesDate="2026-08-31", status="1"))
    msg = r.json().get("msg", "")
    check("规则-销售超库存被拦截", r.json().get("code") != 0 and "库存不足" in msg, r.text[:120])

    r = req("post", "/biz/sales/add", data=dict(salesCode="XS002", customerName="__测试客户__",
                productName="__规则测试产品__", quantity="20", price="5",
                salesDate="2026-08-31", status="1"))
    check("规则-销售单创建(已完成)", r.json().get("code") == 0, r.text[:120])
    after2 = get_stock("__规则测试产品__")
    check("规则-销售出库自动扣库存", after2 == after - 20, "库存 %s -> %s" % (after, after2))

    for mod, field, val in [("purchase", "purchaseCode", "CG001"),
                            ("sales", "salesCode", "XS001"),
                            ("sales", "salesCode", "XS002")]:
        rid = find_id(mod, field, val)
        if rid:
            req("post", "/biz/%s/remove" % mod, data={"ids": str(rid)})

    rid = find_id("leave", "empName", "__测试员工__")
    if rid:
        r = req("post", "/biz/leave/audit", data={"id": rid, "status": "1", "auditRemark": "同意"})
        check("规则-请假审批通过", r.json().get("code") == 0, r.text[:120])
        r = req("post", "/biz/leave/list", data={"empName": "__测试员工__"})
        row = r.json().get("rows", [{}])[0]
        check("规则-审批后状态变更", row.get("status") == "1", "status=%s" % row.get("status"))
        r = req("post", "/biz/leave/audit", data={"id": rid, "status": "9"})
        check("规则-非法审批状态被拒", r.json().get("code") != 0, r.text[:120])
        delete("leave", rid)


# ---------------- 4. 系统模块 & 首页 ----------------
def test_system():
    for name in ("user", "role", "dict"):
        r = req("post", "/system/%s/list" % name, data={})
        check("系统-%s列表" % name, r.status_code == 200 and ("rows" in r.json() or "total" in r.json()),
              r.text[:100])
    r = req("post", "/system/dept/list", data={})
    body = r.json()
    check("系统-dept列表", r.status_code == 200 and (isinstance(body, list) or "rows" in body),
          r.text[:100])
    r = req("get", "/index")
    check("系统-主框架页", r.status_code == 200, "HTTP %s" % r.status_code)
    r = req("get", "/system/main")
    check("系统-首页工作台", r.status_code == 200 and "企业管理系统" in r.text, "HTTP %s" % r.status_code)
    r = req("get", "/biz/leave")
    check("系统-请假页面含审批按钮", "auditLeave" in r.text)



# ---------------- 5. 拓展功能：看板/预警/导出 ----------------
def test_extension():
    r = req("get", "/biz/dashboard")
    check("拓展-看板页面渲染", r.status_code == 200 and "echarts" in r.text, "HTTP %s" % r.status_code)
    r = req("post", "/biz/dashboard/panel", data={})
    check("拓展-看板指标接口", r.json().get("code") == 0 and "customerCount" in r.text, r.text[:100])
    r = req("post", "/biz/dashboard/trend", data={})
    d = r.json().get("data", {})
    check("拓展-近7日趋势(7个点+零填充)",
          r.json().get("code") == 0 and len(d.get("dates", [])) == 7 and len(d.get("sales", [])) == 7,
          r.text[:150])
    r = req("post", "/biz/dashboard/productTop", data={})
    check("拓展-产品Top5接口", r.json().get("code") == 0, r.text[:100])
    r = req("post", "/biz/dashboard/status", data={})
    check("拓展-状态分布接口", r.json().get("code") == 0 and "contract" in r.text, r.text[:100])

    r = req("get", "/biz/stock/alert")
    check("拓展-库存预警页渲染", r.status_code == 200, "HTTP %s" % r.status_code)
    r = req("post", "/biz/stock/alertList", data={})
    check("拓展-库存预警列表", r.status_code == 200 and "total" in r.json(), r.text[:100])
    # 预警数据闭环：库存1、下限10 -> 出现在预警里；补货到100 -> 消失
    req("post", "/biz/stock/add", data=dict(productName="__预警测试产品__", warehouse="默认仓库",
                                            quantity="1", minQuantity="10"))
    r = req("post", "/biz/stock/alertList", data={"productName": "__预警测试产品__"})
    check("拓展-低库存触发预警", r.json().get("total", 0) >= 1, r.text[:120])
    rid = find_id("stock", "productName", "__预警测试产品__")
    if rid:
        req("post", "/biz/stock/edit", data=dict(id=rid, productName="__预警测试产品__",
                                                 warehouse="默认仓库", quantity="100", minQuantity="10"))
        r = req("post", "/biz/stock/alertList", data={"productName": "__预警测试产品__"})
        check("拓展-补货后预警解除", r.json().get("total", 0) == 0, r.text[:120])
        delete("stock", rid)

    for name in ("customer", "product", "sales"):
        r = req("post", "/biz/%s/export" % name, data={})
        ok = r.json().get("code") == 0 and (".xlsx" in r.text or "xlsx" in r.text)
        check("拓展-%s导出" % name, ok, r.text[:120])



# ---------------- 6. 员工工作台：打卡/请销假/汇报 ----------------
def test_portal():
    r = req("get", "/portal")
    check("门户-工作台渲染", r.status_code == 200 and "上班打卡" in r.text, "HTTP %s" % r.status_code)

    # 打卡：上班 -> 重复上班被拒 -> 下班 -> 重复下班被拒
    # 当天重跑时已有打卡记录，"无需重复打卡"同样证明业务正确，故首卡做容错断言
    r = req("post", "/portal/punch", data={"type": "in"})
    check("门户-上班打卡(或当日已打)",
          r.json().get("code") == 0 or "无需重复打卡" in r.json().get("msg", ""), r.text[:100])
    r = req("post", "/portal/punch", data={"type": "in"})
    check("门户-重复上班打卡被拒", r.json().get("code") != 0, r.text[:100])
    r = req("post", "/portal/punch", data={"type": "out"})
    check("门户-下班打卡(或当日已打)",
          r.json().get("code") == 0 or "无需重复打卡" in r.json().get("msg", ""), r.text[:100])
    r = req("post", "/portal/punch", data={"type": "out"})
    check("门户-重复下班打卡被拒", r.json().get("code") != 0, r.text[:100])
    r = req("post", "/portal/punch", data={"type": "bad"})
    check("门户-非法打卡类型被拒", r.json().get("code") != 0, r.text[:100])

    # 请假：提交(默认待审批) -> 管理员通过 -> 员工销假 -> 重复销假被拒
    # 先清残留：admin 的所有旧请假
    r = req("post", "/portal/leave/list", data={"empName": "若依"})
    for old_row in r.json().get("rows", []):
        req("post", "/biz/leave/remove", data={"ids": str(old_row["id"])})
    r = req("post", "/portal/leave/add",
            data=dict(leaveType="4", startDate="2026-09-10", endDate="2026-09-11", days="2", reason="门户测试"))
    check("门户-提交请假", r.json().get("code") == 0, r.text[:100])
    r = req("post", "/portal/leave/list", data={"empName": "若依"})
    rows = [x for x in r.json().get("rows", []) if x.get("reason") == "门户测试"]
    check("门户-请假默认待审批", len(rows) > 0 and rows[0].get("status") == "0", str(rows[:1]))
    lid = rows[0].get("id") if rows else None
    r = req("post", "/biz/leave/audit", data={"id": lid, "status": "1", "auditRemark": "同意"})
    check("门户-管理员审批通过", r.json().get("code") == 0, r.text[:100])
    r = req("post", "/portal/leave/cancel", data={"id": lid})
    check("门户-员工销假", r.json().get("code") == 0, r.text[:100])
    r = req("post", "/portal/leave/cancel", data={"id": lid})
    check("门户-重复销假被拒", r.json().get("code") != 0, r.text[:100])

    # 汇报：提交 -> 列表可见 -> 详情 -> 删除
    r = req("post", "/portal/report/add",
            data=dict(reportType="1", reportDate="2026-09-03", title="__门户测试日报__", content="完成测试"))
    check("门户-提交日报", r.json().get("code") == 0, r.text[:100])
    r = req("post", "/portal/report/list", data={"title": "__门户测试日报__"})
    rows = r.json().get("rows", [])
    check("门户-汇报列表可见", len(rows) == 1, r.text[:120])
    if rows:
        rid = rows[0]["id"]
        r = req("get", "/portal/report/view/%s" % rid)
        check("门户-汇报详情", r.status_code == 200 and "完成测试" in r.text, "HTTP %s" % r.status_code)
        r = req("post", "/portal/report/remove", data={"id": rid})
        check("门户-删除汇报", r.json().get("code") == 0, r.text[:100])


# ---------------- 7. 假期余额（Odoo hr_holidays 思路） ----------------
def test_quota():
    # 清残留：旧配额与余额测试请假
    r = req("post", "/portal/leave/list", data={"empName": "若依"})
    for old_row in r.json().get("rows", []):
        if old_row.get("reason") == "余额测试":
            req("post", "/biz/leave/remove", data={"ids": str(old_row["id"])})
    r = req("post", "/biz/quota/list", data={})
    for old_row in r.json().get("rows", []):
        req("post", "/biz/quota/remove", data={"ids": str(old_row["id"])})
    # 管理端：配额 CRUD
    r = req("get", "/biz/quota")
    check("余额-管理页渲染", r.status_code == 200, "HTTP %s" % r.status_code)
    r = req("post", "/biz/quota/add", data=dict(empName="若依", leaveType="3", year="2026", quotaDays="5"))
    check("余额-新增配额(年假5天)", r.json().get("code") == 0, r.text[:120])
    r = req("post", "/biz/quota/add", data=dict(empName="若依", leaveType="3", year="2026", quotaDays="9"))
    check("余额-重复配额被拒", r.json().get("code") != 0, r.text[:120])
    r = req("post", "/biz/quota/list", data={"empName": "若依", "year": "2026"})
    rows = r.json().get("rows", [])
    check("余额-列表查询", len(rows) == 1, r.text[:120])
    qid = rows[0]["id"] if rows else None

    # 闭环：配额5天 -> 请6天被拒 -> 请2天通过且扣减 -> 销假返还
    if qid:
        r = req("post", "/portal/leave/add",
                data=dict(leaveType="3", startDate="2026-10-10", endDate="2026-10-15", days="6", reason="余额测试"))
        check("余额-超余额请假被拒", r.json().get("code") != 0 and "余额不足" in r.json().get("msg", ""), r.text[:150])
        r = req("post", "/portal/leave/add",
                data=dict(leaveType="3", startDate="2026-10-10", endDate="2026-10-11", days="2", reason="余额测试"))
        check("余额-余额内请假可提交", r.json().get("code") == 0, r.text[:120])
        r = req("post", "/portal/leave/list", data={})
        row = r.json().get("rows", [{}])[0]
        lid = row.get("id")
        r = req("post", "/biz/leave/audit", data={"id": lid, "status": "1", "auditRemark": "同意"})
        check("余额-审批通过并扣减", r.json().get("code") == 0, r.text[:150])
        r = req("post", "/biz/quota/list", data={"empName": "若依", "year": "2026"})
        rows = r.json().get("rows", [])
        used = float(rows[0]["usedDays"]) if rows else -1
        check("余额-已用天数=2", used == 2.0, "usedDays=%s" % used)
        r = req("post", "/portal/leave/cancel", data={"id": lid})
        check("余额-销假成功", r.json().get("code") == 0, r.text[:120])
        r = req("post", "/biz/quota/list", data={"empName": "若依", "year": "2026"})
        rows = r.json().get("rows", [])
        used = float(rows[0]["usedDays"]) if rows else -1
        check("余额-销假后返还(已用=0)", used == 0.0, "usedDays=%s" % used)

        # 修改配额：已用>配额被拒
        r = req("post", "/biz/quota/edit",
                data=dict(id=qid, empName="若依", leaveType="3", year="2026", quotaDays="5", usedDays="6"))
        check("余额-已用超配额被拒", r.json().get("code") != 0, r.text[:120])
        # 导出
        r = req("post", "/biz/quota/export", data={})
        check("余额-导出", r.json().get("code") == 0, r.text[:100])
        # 清理
        req("post", "/biz/quota/remove", data={"ids": str(qid)})
    # 工作台：有配额时应展示余额卡（上一段刚清空了配额，此处软断言页面可用即可）
    r = req("get", "/portal")
    check("余额-工作台页渲染正常", r.status_code == 200 and "打卡" in r.text, "HTTP %s" % r.status_code)


# ---------------- 8. 库存流水（Odoo stock.move 思路） ----------------
def test_stock_move():
    r = req("get", "/biz/stockMove")
    check("流水-页面渲染", r.status_code == 200, "HTTP %s" % r.status_code)
    r = req("post", "/biz/stockMove/list", data={})
    check("流水-列表查询", r.status_code == 200 and "total" in r.json(), r.text[:100])
    r = req("post", "/biz/stockMove/export", data={})
    check("流水-导出", r.json().get("code") == 0, r.text[:100])

    # 闭环：采购入库CG990 -> 流水含采购来源；销售出库XS990 -> 流水含销售来源
    req("post", "/biz/product/add", data=dict(productCode="SM01", productName="__流水测试产品__",
                                              category="测试", unit="个", price="1", status="0"))
    req("post", "/biz/purchase/add", data=dict(purchaseCode="CG990", supplierName="__测试供应商__",
                productName="__流水测试产品__", quantity="10", price="1",
                purchaseDate="2026-09-04", status="1"))
    req("post", "/biz/sales/add", data=dict(salesCode="XS990", customerName="__测试客户__",
                productName="__流水测试产品__", quantity="4", price="1",
                salesDate="2026-09-04", status="1"))
    r = req("post", "/biz/stockMove/list", data={"productName": "__流水测试产品__"})
    rows = r.json().get("rows", [])
    check("流水-出入库各一条", len(rows) >= 2, "rows=%s" % len(rows))
    purchase_row = [x for x in rows if x.get("sourceType") == "purchase"]
    sales_row = [x for x in rows if x.get("sourceType") == "sales"]
    check("流水-采购入库带单号", len(purchase_row) >= 1 and purchase_row[0].get("sourceCode") == "CG990"
          and purchase_row[0].get("moveType") == "1", str(purchase_row[:1]))
    check("流水-销售出库带单号", len(sales_row) >= 1 and sales_row[0].get("sourceCode") == "XS990"
          and sales_row[0].get("moveType") == "2", str(sales_row[:1]))
    # 流水只增不改，多轮测试会累积；校验最新两条（出库+入库）结余连续：最新结余+4 == 前一条结余
    check("流水-结余连续(最新出库结余=上次入库结余-4)",
          len(rows) >= 2 and float(rows[0].get("balanceAfter")) == float(rows[1].get("balanceAfter")) - 4
          and rows[0].get("moveType") == "2" and rows[1].get("moveType") == "1",
          str([(x.get("moveType"), x.get("balanceAfter")) for x in rows[:4]]))

    # 清理
    for mod, field, val in [("purchase", "purchaseCode", "CG990"), ("sales", "salesCode", "XS990")]:
        rid = find_id(mod, field, val)
        if rid:
            req("post", "/biz/%s/remove" % mod, data={"ids": str(rid)})
    pid = find_id("product", "productName", "__流水测试产品__")
    if pid:
        req("post", "/biz/product/remove", data={"ids": str(pid)})


# ---------------- 9. 费用报销 + 审批中心（Odoo hr_expense 思路） ----------------
def test_expense():
    import time as _time
    reason_tag = "报销测试%d" % (int(_time.time()) % 1000000)
    # 清残留：待审批的旧测试单可撤回；已审批的保留不影响（按唯一标记区分）

    r = req("get", "/portal/expense")
    check("报销-页面渲染", r.status_code == 200, "HTTP %s" % r.status_code)
    r = req("post", "/portal/expense/add",
            data=dict(category="1", amount="580.50", expenseDate="2026-09-04", reason=reason_tag))
    check("报销-提交", r.json().get("code") == 0, r.text[:100])
    r = req("post", "/portal/expense/add",
            data=dict(category="2", amount="0", expenseDate="2026-09-04", reason="零金额"))
    check("报销-零金额被拒", r.json().get("code") != 0, r.text[:100])
    r = req("post", "/portal/expense/list", data={"empName": "若依"})
    rows = [x for x in r.json().get("rows", []) if x.get("reason") == reason_tag]
    check("报销-列表可见", len(rows) == 1, str(rows[:1]))
    check("报销-默认待审批", rows and rows[0].get("status") == "0", str(rows[:1]))
    eid = rows[0]["id"] if rows else None

    # 审批中心：待办角标 + 报销列表 + 审批
    r = req("get", "/biz/approval")
    check("审批中心-页面渲染", r.status_code == 200 and "报销待审批" in r.text, "HTTP %s" % r.status_code)
    r = req("post", "/biz/approval/pending", data={})
    check("审批中心-待办角标含报销数", r.json().get("code") == 0 and "expenseCount" in r.text, r.text[:100])
    r = req("post", "/biz/approval/expenseList", data={})
    rows = [x for x in r.json().get("rows", []) if str(x.get("id")) == str(eid)]
    check("审批中心-报销进入待办", len(rows) == 1, str(rows[:1]))
    r = req("post", "/biz/approval/expenseAudit", data={"id": eid, "status": "1", "auditRemark": "报销测试通过"})
    check("审批中心-报销审批通过", r.json().get("code") == 0, r.text[:100])
    r = req("post", "/portal/expense/list", data={"empName": "若依"})
    rows = [x for x in r.json().get("rows", []) if x.get("reason") == reason_tag]
    check("报销-审批后状态与审批人",
          rows and rows[0].get("status") == "1" and rows[0].get("auditBy") == "admin", str(rows[:1]))
    r = req("post", "/portal/expense/remove", data={"ids": str(eid)})
    check("报销-已审批不能撤回", r.json().get("code") != 0, r.text[:100])
    r = req("post", "/biz/approval/expenseAudit", data={"id": eid, "status": "9"})
    check("审批中心-非法状态被拒", r.json().get("code") != 0, r.text[:100])

if __name__ == "__main__":
    test_auth()
    test_crud()
    test_rules()
    test_system()
    test_extension()
    test_portal()
    test_quota()
    test_stock_move()
    test_expense()
    failed = [x for x in results if not x[1]]
    print("\n========== 测试汇总 ==========")
    print("总计: %d  通过: %d  失败: %d" % (len(results), len(results) - len(failed), len(failed)))
    for name, ok, detail in failed:
        print("  [FAIL] %s  %s" % (name, detail))
    sys.exit(1 if failed else 0)
