# -*- coding: utf-8 -*-
"""IM 实时推送端到端验证（在服务器上对 127.0.0.1 跑）

B（testuser02）先建立 WS 长连接；A（admin）经 HTTP 发一条私信；
测 B 的 WS 连接收到推送帧的延迟；再验证 B 不刷新页面即可拉到该消息。
"""
import os
import ipaddress
import json
import socket
import time
import urllib.parse

import requests
import websocket

BASE = "http://127.0.0.1:48080/admin-api"
WS_BASE = "ws://127.0.0.1:48080/infra/ws"
TENANT = "1"
ALLOWED_HOSTS = {"127.0.0.1"}


def guard(url):
    """统一边界校验：仅 http/ws + 回环主机 + 解析后 IP 必须为回环（防 SSRF）"""
    parsed = urllib.parse.urlparse(url)
    assert parsed.scheme in ("http", "ws"), "仅允许 http/ws 协议"
    assert parsed.hostname in ALLOWED_HOSTS, "仅允许本地被测服务"
    ip = socket.gethostbyname(parsed.hostname)
    assert ipaddress.ip_address(ip).is_loopback, "解析后地址必须为回环地址"


def login(username, password):
    url = BASE + "/system/auth/login"
    guard(url)
    r = requests.post(url, json={"username": username, "password": password},
                      headers={"tenant-id": TENANT}, timeout=15, allow_redirects=False)
    j = r.json()
    assert j["code"] == 0, j
    return j["data"]


def main():
    a = login(os.environ.get("BIZ_TEST_ADMIN_USER", "admin"), os.environ["BIZ_TEST_ADMIN_PASSWORD"])
    b = login(os.environ["BIZ_TEST_PEER_USER"], os.environ["BIZ_TEST_PEER_PASSWORD"])
    b_uid = b["userId"]

    # 1. B 建立 WS 长连接（与前端同款：refreshToken 鉴权）
    ws_url = WS_BASE + "?token=" + b["refreshToken"]
    guard(ws_url)
    ws = websocket.create_connection(ws_url, header={"tenant-id": TENANT}, timeout=10)
    print("[1] B 的 WebSocket 握手成功")

    # 2. A 经 HTTP 发私信，同时计时
    t0 = time.time()
    payload = {"clientMessageId": "rt-%d" % int(t0 * 1000),
               "receiverId": b_uid, "type": 101,
               "content": json.dumps({"content": "实时推送测速"})}
    send_url = BASE + "/im/message/private/send"
    guard(send_url)
    r = requests.post(send_url, json=payload,
                      headers={"tenant-id": TENANT,
                               "Authorization": "Bearer " + a["accessToken"]}, timeout=15, allow_redirects=False)
    assert r.json()["code"] == 0, r.text[:200]
    send_done = time.time() - t0

    # 3. B 的 WS 收推送帧（最多等 5 秒）
    received, latency = None, None
    ws.settimeout(5)
    deadline = time.time() + 5
    while time.time() < deadline:
        frame = ws.recv()
        if not frame or frame == "pong":
            continue
        received = frame
        latency = time.time() - t0
        break
    print("[2] HTTP 发送耗时 %.0f ms" % (send_done * 1000))
    if received:
        print("[3] B 的 WebSocket 收到推送，距发送 %.0f ms" % (latency * 1000))
        print("    帧内容(截断): %s" % received[:200])
    else:
        print("[3] 5 秒内未收到推送帧")

    # 4. B 不刷新，直接拉取（模拟前端拉取兜底）
    pull_url = BASE + "/im/message/private/pull"
    guard(pull_url)
    r = requests.get(pull_url, params={"minId": 0, "size": 10},
                     headers={"tenant-id": TENANT,
                              "Authorization": "Bearer " + b["accessToken"]}, timeout=15, allow_redirects=False)
    hit = any("实时推送测速" in str(m.get("content")) for m in r.json().get("data", []))
    print("[4] B 拉取可见该消息: %s" % hit)

    ws.close()
    print("结论:", "实时推送链路正常" if received else "推送链路仍断")


if __name__ == "__main__":
    main()
