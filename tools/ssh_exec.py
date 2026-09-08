#!/usr/bin/env python3
"""阿里云 ECS 远程执行命令的小工具（paramiko）。

用法：
  # 密码方式（密码走环境变量，避免进 shell 历史）
  ALIYUN_PW=xxx python tools/ssh_exec.py --host <公网IP> "uname -a"

  # 密钥方式
  python tools/ssh_exec.py --host <公网IP> --key ~/.ssh/id_temp_deploy "uname -a"

  # 多条命令直接用 && 或分号写在一条里；超时默认 60s
"""
import argparse
import os
import sys

import paramiko

KNOWN_HOSTS = os.path.expanduser("~/.ssh/known_hosts")


def connect(host, port, user, password, key_file):
    """TOFU 信任策略：known_hosts 里已有该主机则严格校验指纹，首连记录指纹。"""
    client = paramiko.SSHClient()
    client.load_system_host_keys(KNOWN_HOSTS)
    seen = False
    try:
        client.get_host_keys().lookup(host)
        seen = True
    except KeyError:
        pass

    if seen:
        client.set_missing_host_key_policy(paramiko.RejectPolicy())
    else:
        # 首次连接：记录指纹（accept-new），之后每次都严格校验
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    client.connect(
        hostname=host, port=port, username=user,
        password=password,
        key_filename=key_file,
        timeout=15,
    )
    if not seen:  # 把首连指纹落盘
        try:
            keys = client.get_host_keys()
            with open(KNOWN_HOSTS, "a", encoding="utf-8") as f:
                for k in keys.lookup(host).values():
                    f.write("%s %s\n" % (host.lower(), k.get_base64()))
        except OSError:
            pass
    return client


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--host", required=True)
    p.add_argument("--port", type=int, default=22)
    p.add_argument("--user", default="root")
    p.add_argument("--password", default=None, help="优先用环境变量 ALIYUN_PW")
    p.add_argument("--key", default=None, help="私钥路径，如 ~/.ssh/id_temp_deploy")
    p.add_argument("--timeout", type=int, default=60)
    p.add_argument("command")
    args = p.parse_args()

    password = args.password or os.environ.get("ALIYUN_PW")
    key_file = os.path.expanduser(args.key) if args.key else None
    client = connect(args.host, args.port, args.user, password, key_file)
    stdin, stdout, stderr = client.exec_command(args.command, timeout=args.timeout)
    out = stdout.read().decode("utf-8", "replace")
    err = stderr.read().decode("utf-8", "replace")
    code = stdout.channel.recv_exit_status()
    if out:
        print(out, end="")
    if err:
        print(err, end="", file=sys.stderr)
    client.close()
    sys.exit(code)


if __name__ == "__main__":
    main()
