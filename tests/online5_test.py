"""5 账号并发登录 + 在线检测验证（在服务器上对 127.0.0.1 运行）

场景：admin + 4 个临时账号 = 5 个不同账号同时登录；
验证：在线列表计数、同账号重复登录互踢、强制下线、被踢后可重登。
用法：python3 online5_test.py
"""
import ipaddress
import requests
import socket
import time
import sys
import urllib.parse

BASE = "http://127.0.0.1:48080/admin-api"
TENANT = "1"
ALLOWED_HOSTS = {"127.0.0.1"}
results = []


def req(session, method, path, **kwargs):
    """统一请求入口：协议 http、主机限回环（防 SSRF）"""
    url = BASE + path
    parsed = urllib.parse.urlparse(url)
    assert parsed.scheme == "http", "仅允许 http 协议"
    assert parsed.hostname in ALLOWED_HOSTS, "仅允许本地被测服务"
    ip = socket.gethostbyname(parsed.hostname)
    assert ipaddress.ip_address(ip).is_loopback, "解析后地址必须为回环地址"
    return getattr(session, method)(url, timeout=15, **kwargs)


def check(name, ok, detail=""):
    results.append((name, ok, detail))
    print(("[PASS] " if ok else "[FAIL] ") + name + ("" if ok else "  " + detail))


def login(session, username, password):
    r = req(session, "post", "/system/auth/login",
            json={"username": username, "password": password},
            headers={"tenant-id": TENANT})
    return r.json()


def auth_headers(token):
    return {"tenant-id": TENANT, "Authorization": "Bearer " + token}


def main():
    stamp = str(int(time.time()))[-7:]
    admin = requests.Session()

    # 0. 管理员登录
    j = login(admin, "admin", os.environ["BIZ_TEST_ADMIN_PASSWORD"])
    check("准备-admin登录", j.get("code") == 0 and j.get("data"), str(j)[:150])
    admin_token = j["data"]["accessToken"]

    # 1. 创建 4 个临时账号
    temps = []
    for i in range(4):
        uname = "cc%s%d" % (stamp, i)
        pwd = "C" + stamp + str(i) + "xQ"
        r = req(admin, "post", "/system/user/create",
                json=dict(username=uname, nickname="并发测试%d" % (i + 1), password=pwd,
                          deptId=103, mobile="139%08d" % (int(time.time()) % 100000000 + i)),
                headers=auth_headers(admin_token))
        check("准备-创建账号%s" % uname, r.json().get("code") == 0, r.text[:150])
        temps.append((uname, pwd, r.json().get("data")))

    # 2. 5 个账号同时登录（各持一个会话）
    sessions = {"admin": admin_token}
    uid_map = {}
    for uname, pwd, uid in temps:
        j = login(requests.Session(), uname, pwd)
        check("并发-账号%s登录" % uname, j.get("code") == 0, str(j)[:150])
        sessions[uname] = j["data"]["accessToken"]
        uid_map[uname] = uid
    r = req(admin, "get", "/system/online-user/list", headers=auth_headers(admin_token))
    online = r.json().get("data", [])
    online_names = [u.get("username") for u in online]
    check("在线检测-5个账号全部在线",
          all(n in online_names for n in ["admin"] + [t[0] for t in temps]),
          str(online_names))
    check("在线检测-单设备：无重复账号会话",
          len(online_names) == len(set(online_names)), str(online_names))
    admin_online = next((u for u in online if u.get("username") == "admin"), {})
    check("在线检测-列表含昵称/部门/时间",
          bool(admin_online.get("nickname")) and bool(admin_online.get("deptName"))
          and bool(admin_online.get("createTime")), str(admin_online))

    # 3. admin 在别处重复登录 → 旧会话被踢，在线仍为 5
    j = login(requests.Session(), "admin", os.environ["BIZ_TEST_ADMIN_PASSWORD"])
    check("互踢-admin二次登录", j.get("code") == 0, str(j)[:150])
    r = req(requests, "get", "/system/auth/get-permission-info",
            headers=auth_headers(sessions["admin"]))
    check("互踢-旧admin会话已失效", r.json().get("code") != 0, r.text[:150])
    admin_token2 = j["data"]["accessToken"]
    r = req(requests, "get", "/system/online-user/list", headers=auth_headers(admin_token2))
    online_names2 = [u.get("username") for u in r.json().get("data", [])]
    check("互踢-在线数仍为5(单会话)", len(online_names2) == 5, str(online_names2))

    # 4. 强制下线第一个临时账号 → 在线 4，其会话失效
    r = req(admin, "delete", "/system/online-user/kick",
            params={"userId": uid_map[temps[0][0]], "userType": 2},
            headers=auth_headers(admin_token2))
    check("下线-强制下线接口", r.json().get("code") == 0, r.text[:150])
    r = req(requests, "get", "/system/auth/get-permission-info",
            headers=auth_headers(sessions[temps[0][0]]))
    check("下线-被踢会话已失效", r.json().get("code") != 0, r.text[:150])
    r = req(requests, "get", "/system/online-user/list", headers=auth_headers(admin_token2))
    check("下线-在线数变为4", len(r.json().get("data", [])) == 4,
          str([u.get("username") for u in r.json().get("data", [])]))

    # 5. 被踢账号可重新登录
    j = login(requests.Session(), temps[0][0], temps[0][1])
    check("重登-被踢账号可重新登录", j.get("code") == 0, str(j)[:150])

    # 6. 清理临时账号
    for uname, pwd, uid in temps:
        req(admin, "delete", "/system/user/delete", params={"id": uid},
            headers=auth_headers(admin_token2))
    print("\n========== 汇总 ==========")
    failed = [x for x in results if not x[1]]
    print("总计: %d  通过: %d  失败: %d" % (len(results), len(results) - len(failed), len(failed)))
    for name, ok, detail in failed:
        print("  [FAIL] %s  %s" % (name, detail))
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
