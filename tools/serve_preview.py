#!/usr/bin/env python3
"""本地预览服务：静态服务构建产物 + 反向代理 /admin-api 到后端。

用途：uni-app H5 的 dev 模式（`npm run dev:h5`）自带 /admin-api 代理，但
`dist/build/h5` 的构建产物是纯静态的——直接起静态服务会**登录不了**
（请求 /admin-api 返回 404/SPA 兜底），容易误判成"前端坏了"。
本脚本让构建产物也能真实登录联调。

用法：
    python tools/serve_preview.py                 # 默认 8900 端口，代理到生产
    python tools/serve_preview.py --port 8900 --backend http://8.155.128.225

访问：http://127.0.0.1:8900/m/
注意：产物 index.html 引用 /m/assets/...（manifest 里 router.base=/m/），
所以必须通过 /m/ 路径访问，不能直接访问根。
"""
import argparse
import http.server
import os
import socketserver
import urllib.error
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT_ROOT = os.path.normpath(os.path.join(HERE, "..", "dist", "build", "h5"))
DEFAULT_BACKEND = "http://8.155.128.225"

HOP_BY_HOP = {
    "host",
    "connection",
    "keep-alive",
    "proxy-authenticate",
    "proxy-authorization",
    "te",
    "trailers",
    "transfer-encoding",
    "upgrade",
    "content-length",
    "content-encoding",
    "accept-encoding",
}

BACKEND = DEFAULT_BACKEND


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=kwargs.pop("directory"), **kwargs)

    # ---------- 反向代理 ----------
    def _proxy(self, method):
        url = BACKEND.rstrip("/") + self.path
        length = int(self.headers.get("Content-Length") or 0)
        body = self.rfile.read(length) if length else None

        req = urllib.request.Request(url, data=body, method=method)
        for key, value in self.headers.items():
            if key.lower() not in HOP_BY_HOP:
                req.add_header(key, value)

        try:
            with urllib.request.urlopen(req, timeout=60) as resp:
                self._relay(resp.status, resp.headers, resp.read())
        except urllib.error.HTTPError as exc:
            self._relay(exc.code, exc.headers, exc.read())
        except Exception as exc:  # 后端不可达
            payload = ('{"code":500,"msg":"preview proxy error: %s"}' % exc).encode("utf-8")
            self._relay(502, {"Content-Type": "application/json"}, payload)

    def _relay(self, status, headers, data):
        self.send_response(status)
        for key, value in headers.items():
            if key.lower() not in HOP_BY_HOP:
                self.send_header(key, value)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        if data:
            self.wfile.write(data)

    def do_GET(self):
        if self.path.startswith("/admin-api"):
            return self._proxy("GET")
        return self._static()

    def do_POST(self):
        return self._proxy("POST")

    def do_PUT(self):
        return self._proxy("PUT")

    def do_DELETE(self):
        return self._proxy("DELETE")

    def do_PATCH(self):
        return self._proxy("PATCH")

    # ---------- 静态 ----------
    def _static(self):
        path = self.path.split("?")[0]
        if path in ("/", ""):
            self.send_response(302)
            self.send_header("Location", "/m/")
            self.end_headers()
            return
        if not path.startswith("/m/"):
            path = "/m" + path
        rel = path[len("/m"):]
        target = os.path.join(self.directory, rel.lstrip("/"))
        # SPA history 回退：无扩展名或文件不存在时交给 index.html
        if not os.path.isfile(target):
            self.path = "/index.html"
        else:
            self.path = path[len("/m"):]
        return super().do_GET()

    def log_message(self, fmt, *args):
        # 只记录代理请求，静态资源太吵
        if self.path.startswith("/admin-api"):
            print("[proxy] %s %s" % (self.command, self.path), flush=True)


def main():
    global BACKEND
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=8900)
    parser.add_argument("--backend", default=DEFAULT_BACKEND)
    parser.add_argument("--root", default=DEFAULT_ROOT)
    args = parser.parse_args()

    BACKEND = args.backend
    root = os.path.abspath(args.root)
    if not os.path.isfile(os.path.join(root, "index.html")):
        raise SystemExit("找不到构建产物 index.html，请先 npm run build:h5：%s" % root)

    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.ThreadingTCPServer(
        ("127.0.0.1", args.port),
        lambda *a, **kw: Handler(*a, directory=root, **kw),
    ) as httpd:
        print("preview  http://127.0.0.1:%d/m/" % args.port, flush=True)
        print("backend  %s" % BACKEND, flush=True)
        print("root     %s" % root, flush=True)
        httpd.serve_forever()


if __name__ == "__main__":
    main()
