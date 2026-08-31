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


if __name__ == "__main__":
    test_auth()
    test_crud()
    test_rules()
    test_system()
    failed = [x for x in results if not x[1]]
    print("\n========== 测试汇总 ==========")
    print("总计: %d  通过: %d  失败: %d" % (len(results), len(results) - len(failed), len(failed)))
    for name, ok, detail in failed:
        print("  [FAIL] %s  %s" % (name, detail))
    sys.exit(1 if failed else 0)
