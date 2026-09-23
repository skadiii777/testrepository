#!/usr/bin/env python3
"""桌面端（enterprise-pro-ui）模块级接口冒烟测试。

路径取自 `enterprise-pro-ui/src/api/**/index.ts` 的真实定义。
**只跑只读查询类接口**（page / list / panel / 报表），不做任何写操作。

用法：
    python tools/test_modules.py --username admin --password <pwd>
    python tools/test_modules.py --base http://8.155.128.225/admin-api --username admin --password <pwd>
"""
import argparse
import json
import sys
import urllib.error
import urllib.request

DEFAULT_BASE = "http://localhost:48080/admin-api"

# (显示名, HTTP 方法, 路径, 是否需要分页参数)
GET_MODULES = [
    # ---- CRM / 客户合同产品 ----
    ("客户管理", "/biz/customer/page"),
    ("产品管理", "/biz/product/page"),
    ("合同管理", "/biz/contract/page"),
    ("供应商管理", "/biz/supplier/page"),
    ("线索管理", "/biz/clue/page"),
    ("商机管理", "/biz/business/page"),
    ("联系人管理", "/biz/contact/page"),
    ("客户跟进", "/biz/followup/page"),
    # ---- 进销存 ----
    ("采购单", "/biz/purchase/page"),
    ("销售单", "/biz/sales/page"),
    ("库存", "/biz/stock/page"),
    ("库存流水", "/biz/stockmove/page"),
    ("库存盘点", "/biz/stockcheck/page"),
    ("退货管理", "/biz/return/page"),
    ("收付款管理", "/biz/payment/page"),
    ("业绩目标", "/biz/target/page"),
    # ---- 人事考勤 ----
    ("员工管理", "/biz/employee/page"),
    ("考勤记录", "/biz/attendance/page"),
    ("请假管理", "/biz/leave/page"),
    ("假期余额", "/biz/quota/page"),
    ("补卡管理", "/biz/correction/page"),
    # ---- 协作审批 ----
    ("报销管理", "/biz/expense/page"),
    ("业务汇报", "/biz/report/page"),
    ("公司公告", "/biz/announcement/page"),
    # ---- 财务 FMS ----
    ("会计科目", "/biz/fms/account/page"),
    ("记账凭证", "/biz/fms/voucher/page"),
    ("科目余额表", "/biz/fms/voucher/balance"),
    ("财务报表", "/biz/fms/voucher/report"),
    # ---- 仓储 WMS ----
    ("库位管理", "/biz/wms/location/page"),
    ("库位库存", "/biz/wms/stock/page"),
    ("库位流水", "/biz/wms/stock/move-page"),
    ("上架任务", "/biz/wms/stock/task-page?type=putaway"),
    # ---- 审批中心 ----
    ("审批-请假待办", "/biz/approval/leave-page"),
    ("审批-报销待办", "/biz/approval/expense-page"),
    ("审批-补卡待办", "/biz/approval/correction-page"),
    # ---- 系统管理 ----
    ("系统-用户", "/system/user/page"),
    ("系统-角色", "/system/role/page"),
    ("系统-字典类型", "/system/dict-type/page"),
    ("系统-部门", "/system/dept/list"),
    ("系统-菜单", "/system/menu/list"),
    # ---- 员工自助 / IM ----
    ("门户首页数据", "/portal/index-data"),
    ("IM 好友列表", "/im/friend/list"),
]

POST_MODULES = [
    ("看板-指标卡", "/biz/dashboard/panel"),
    ("看板-状态分布", "/biz/dashboard/status"),
    ("审批待办计数", "/biz/approval/pending"),
]


def call(base, method, path, body=None, token=None, tenant="1", timeout=30):
    req = urllib.request.Request(base + path, method=method)
    req.add_header("tenant-id", tenant)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    if body is not None:
        req.data = json.dumps(body).encode("utf-8")
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw = resp.read().decode("utf-8", "replace")
            try:
                return resp.status, json.loads(raw)
            except json.JSONDecodeError:
                return resp.status, raw
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode("utf-8", "replace")
        try:
            return exc.code, json.loads(raw)
        except json.JSONDecodeError:
            return exc.code, raw
    except Exception as exc:
        return 0, str(exc)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default=DEFAULT_BASE)
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", required=True)
    args = parser.parse_args()
    base = args.base.rstrip("/")

    print("=== 目标 %s ===\n" % base, flush=True)

    status, payload = call(base, "POST", "/system/auth/login",
                           {"username": args.username, "password": args.password})
    if not isinstance(payload, dict) or payload.get("code") != 0:
        print("登录失败：HTTP %s %s" % (status, str(payload)[:200]), flush=True)
        return 1
    token = (payload.get("data") or {}).get("accessToken")
    print("登录成功 admin  userId=%s\n" % (payload.get("data") or {}).get("userId"), flush=True)

    ok = fail = 0
    failures = []

    def handle(label, path, http_status, pl):
        nonlocal ok, fail
        code = pl.get("code") if isinstance(pl, dict) else None
        if code == 0:
            data = pl.get("data")
            if isinstance(data, dict) and "total" in data:
                detail = "total=%s" % data.get("total")
            elif isinstance(data, list):
                detail = "%d 条" % len(data)
            elif isinstance(data, dict):
                detail = "%d 字段" % len(data)
            else:
                detail = str(data)[:40]
            print("  PASS  %-18s %-42s %s" % (label, path, detail), flush=True)
            ok += 1
        else:
            msg = str(pl.get("msg") if isinstance(pl, dict) else pl)[:70]
            print("  FAIL  %-18s %-42s HTTP=%s code=%s %s" % (label, path, http_status, code, msg), flush=True)
            fail += 1
            failures.append("%s (%s) -> %s" % (label, path, msg))

    for label, path in GET_MODULES:
        sep = "&" if "?" in path else "?"
        full = path + sep + "pageNo=1&pageSize=5"
        if path.endswith("/balance") or path.endswith("/report") or path.endswith("/list") or path.endswith("/index-data"):
            full = path
        if path == "/im/friend/list":
            full = path
        st, pl = call(base, "GET", full, token=token)
        handle(label, path, st, pl)

    for label, path in POST_MODULES:
        st, pl = call(base, "POST", path, {}, token=token)
        handle(label, path, st, pl)

    print("\n=== 汇总：%d 通过 / %d 失败 ===" % (ok, fail), flush=True)
    if failures:
        print("失败明细：", flush=True)
        for f in failures:
            print("  - " + f, flush=True)
    return 0 if fail == 0 else 2


if __name__ == "__main__":
    sys.exit(main())
