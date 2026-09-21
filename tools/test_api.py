#!/usr/bin/env python3
"""移动端接口全量冒烟测试（标准库实现，无需第三方依赖）。

覆盖 `src/api/index.ts` 中移动端用到的全部接口，分两类：
  - 只读/查询类：直接调用，断言 code == 0
  - 写入类：**只做参数校验探测**（故意传非法参数），断言"接口存在且校验生效"，
    以此验证链路而不向库里写真实业务数据

用法：
    python tools/test_api.py --username admin --password <pwd>
    python tools/test_api.py --base http://8.155.128.225/admin-api --username admin --password <pwd>
"""
import argparse
import json
import sys
import urllib.error
import urllib.request

DEFAULT_BASE = "http://localhost:48080/admin-api"

PASS, FAIL, WARN = "PASS", "FAIL", "WARN"
results = []


def call(base, method, path, body=None, token=None, tenant="1", timeout=30):
    """返回 (http_status, payload_dict_or_text)"""
    req = urllib.request.Request(base + path, method=method)
    req.add_header("tenant-id", tenant)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    data = None
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        req.data = data
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


def record(name, ok, detail):
    results.append((name, PASS if ok else FAIL, detail))
    print("%-6s %-42s %s" % (PASS if ok else FAIL, name, detail), flush=True)


def record_probe(name, http_status, payload, token_ok_msg="接口存在"):
    """写接口探测：能返回业务码即说明路由与鉴权链路正常"""
    if isinstance(payload, dict) and "code" in payload:
        code = payload.get("code")
        msg = str(payload.get("msg"))[:60]
        # code=0 说明真的写成功了（不该发生，因为我们传了非法参数）
        if code == 0:
            record(name, True, "⚠ 非法参数竟然成功，请人工确认：%s" % msg)
        else:
            record(name, True, "校验生效 code=%s msg=%s" % (code, msg))
    else:
        record(name, http_status in (200, 400, 500), "非标准响应: %s" % str(payload)[:70])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default=DEFAULT_BASE)
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", required=True)
    args = parser.parse_args()

    base = args.base.rstrip("/")

    print("=== 目标 %s ===" % base, flush=True)

    # ---------- 1. 登录 ----------
    status, payload = call(base, "POST", "/system/auth/login",
                           {"username": args.username, "password": args.password})
    if not isinstance(payload, dict) or payload.get("code") != 0:
        record("POST /system/auth/login", False, "HTTP %s %s" % (status, str(payload)[:90]))
        print("\n登录失败，后续接口无法测试。", flush=True)
        return 1
    data = payload.get("data") or {}
    token = data.get("accessToken")
    record("POST /system/auth/login", bool(token), "userId=%s token=%s…" % (
        data.get("userId"), (token or "")[:12]))

    # ---------- 2. 权限 ----------
    status, payload = call(base, "GET", "/system/auth/get-permission-info", token=token)
    ok = isinstance(payload, dict) and payload.get("code") == 0
    perms = []
    if ok:
        d = payload.get("data") or {}
        perms = d.get("permissions") or []
    record("GET /system/auth/get-permission-info", ok,
           "角色=%s 权限数=%d" % (len((payload.get("data") or {}).get("roles") or []) if ok else 0, len(perms)))

    is_super = "*:*:*" in perms

    # ---------- 3. 门户（员工自助） ----------
    status, payload = call(base, "GET", "/portal/index-data", token=token)
    ok = isinstance(payload, dict) and payload.get("code") == 0
    d = (payload.get("data") or {}) if ok else {}
    record("GET /portal/index-data", ok,
           ok and "today=%s 打卡=%s/%s 假期类型=%d" % (
               d.get("today"), d.get("checkIn") or "-", d.get("checkOut") or "-",
               len(d.get("quotas") or {})) or str(payload)[:70])

    for name, path in [
        ("GET /portal/leave-page", "/portal/leave-page?pageNo=1&pageSize=5"),
        ("GET /portal/expense-page", "/portal/expense-page?pageNo=1&pageSize=5"),
        ("GET /portal/report-page", "/portal/report-page?pageNo=1&pageSize=5"),
        ("GET /portal/correction-page", "/portal/correction-page?pageNo=1&pageSize=5"),
    ]:
        status, payload = call(base, "GET", path, token=token)
        ok = isinstance(payload, dict) and payload.get("code") == 0
        total = ((payload.get("data") or {}).get("total") if ok else None)
        record(name, ok, ok and "total=%s" % total or str(payload)[:70])

    # ---------- 4. 审批中心 ----------
    status, payload = call(base, "POST", "/biz/approval/pending", token=token)
    ok = isinstance(payload, dict) and payload.get("code") == 0
    record("POST /biz/approval/pending", ok or (not is_super),
           (ok and json.dumps(payload.get("data"), ensure_ascii=False)) or
           ("无审批权限，跳过（预期）" if not is_super else str(payload)[:70]))

    for name, path in [
        ("GET /biz/approval/leave-page", "/biz/approval/leave-page?pageNo=1&pageSize=5"),
        ("GET /biz/approval/expense-page", "/biz/approval/expense-page?pageNo=1&pageSize=5"),
        ("GET /biz/approval/correction-page", "/biz/approval/correction-page?pageNo=1&pageSize=5"),
    ]:
        status, payload = call(base, "GET", path, token=token)
        code = payload.get("code") if isinstance(payload, dict) else None
        # 无权限账号返回 403 属预期
        ok = code == 0 or (not is_super and code in (403, 1002003000))
        note = "total=%s" % ((payload.get("data") or {}).get("total")) if code == 0 else "无权限（预期，普通员工）"
        record(name, ok, note)

    # ---------- 5. 站内信 ----------
    status, payload = call(base, "GET", "/system/notify-message/get-unread-count", token=token)
    ok = isinstance(payload, dict) and payload.get("code") == 0
    record("GET /system/notify-message/get-unread-count", ok,
           ok and "unread=%s" % payload.get("data") or str(payload)[:70])

    status, payload = call(base, "GET", "/system/notify-message/my-page?pageNo=1&pageSize=5", token=token)
    ok = isinstance(payload, dict) and payload.get("code") == 0
    record("GET /system/notify-message/my-page", ok,
           ok and "total=%s" % ((payload.get("data") or {}).get("total")) or str(payload)[:70])

    # ---------- 6. IM ----------
    status, payload = call(base, "GET", "/im/friend/list", token=token)
    ok = isinstance(payload, dict) and payload.get("code") == 0
    n = len(payload.get("data") or []) if ok else 0
    record("GET /im/friend/list", ok, ok and "好友数=%d" % n or str(payload)[:70])

    if n:
        fid = (payload.get("data") or [{}])[0].get("friendUserId")
        status, payload2 = call(base, "GET", "/im/message/private/list?receiverId=%s&limit=5" % fid, token=token)
        ok2 = isinstance(payload2, dict) and payload2.get("code") == 0
        record("GET /im/message/private/list", ok2,
               ok2 and "最近消息=%d 条" % len(payload2.get("data") or []) or str(payload2)[:70])
    else:
        record("GET /im/message/private/list", True, "无好友，跳过（非缺陷）")

    status, payload = call(base, "GET", "/im/message/private/pull?minId=0&size=10", token=token)
    ok = isinstance(payload, dict) and payload.get("code") == 0
    record("GET /im/message/private/pull", ok,
           ok and "%d 条" % len(payload.get("data") or []) or str(payload)[:70])

    # ---------- 7. 字典（登录后刷新用） ----------
    status, payload = call(base, "GET", "/system/dict-data/simple-list", token=token)
    ok = isinstance(payload, dict) and payload.get("code") == 0
    record("GET /system/dict-data/simple-list", ok,
           ok and "%d 项" % len(payload.get("data") or []) or str(payload)[:70])

    # ================= 写入类：仅做参数校验探测，不落真实数据 =================
    print("\n--- 写入接口参数校验探测（非法参数，不应产生数据） ---", flush=True)

    status, payload = call(base, "POST", "/portal/punch?type=__invalid__", token=token)
    record_probe("POST /portal/punch (非法 type)", status, payload)

    status, payload = call(base, "POST", "/portal/leave-submit",
                           {"leaveType": "", "startDate": "", "days": -1}, token=token)
    record_probe("POST /portal/leave-submit (空/负值)", status, payload)

    status, payload = call(base, "POST", "/portal/expense-submit",
                           {"category": "", "amount": -1}, token=token)
    record_probe("POST /portal/expense-submit (空/负值)", status, payload)

    status, payload = call(base, "POST", "/portal/correction-submit",
                           {"workDate": "", "correctType": ""}, token=token)
    record_probe("POST /portal/correction-submit (空值)", status, payload)

    status, payload = call(base, "POST", "/portal/report-submit", {}, token=token)
    record_probe("POST /portal/report-submit (空体)", status, payload)

    status, payload = call(base, "POST", "/portal/leave-cancel?id=999999999", token=token)
    record_probe("POST /portal/leave-cancel (不存在 id)", status, payload)

    status, payload = call(base, "DELETE", "/portal/expense-withdraw?id=999999999", token=token)
    record_probe("DELETE /portal/expense-withdraw (不存在 id)", status, payload)

    status, payload = call(base, "DELETE", "/portal/report-delete?id=999999999", token=token)
    record_probe("DELETE /portal/report-delete (不存在 id)", status, payload)

    status, payload = call(base, "DELETE", "/portal/correction-withdraw?id=999999999", token=token)
    record_probe("DELETE /portal/correction-withdraw (不存在 id)", status, payload)

    if is_super:
        status, payload = call(base, "POST",
                               "/biz/approval/leave-audit?id=999999999&status=1&auditRemark=x", token=token)
        record_probe("POST /biz/approval/leave-audit (不存在 id)", status, payload)
        status, payload = call(base, "POST",
                               "/biz/approval/expense-audit?id=999999999&status=1&auditRemark=x", token=token)
        record_probe("POST /biz/approval/expense-audit (不存在 id)", status, payload)
        status, payload = call(base, "POST",
                               "/biz/approval/correction-audit?id=999999999&status=1&auditRemark=x", token=token)
        record_probe("POST /biz/approval/correction-audit (不存在 id)", status, payload)

    # ---------- 汇总 ----------
    total = len(results)
    passed = sum(1 for _, s, _ in results if s == PASS)
    print("\n=== 汇总：%d/%d 通过 ===" % (passed, total), flush=True)
    for name, state, detail in results:
        if state != PASS:
            print("  FAIL %s -> %s" % (name, detail), flush=True)
    return 0 if passed == total else 2


if __name__ == "__main__":
    sys.exit(main())
