# 阿里云部署指南（enterprise-pro 上线步骤）

> 目标：把「后端 48080 + 前端静态页 + MySQL + Redis」落到阿里云 ECS，边测试边开发。

## 阶段 0 · 服务器与账号准备

1. **ECS**：2核8G 起步（现生产即 2C8G；开发测试最低 2核4G），系统选 Alibaba Cloud Linux 3 或 Ubuntu 22.04
2. **安全组**（ECS 控制台）只放行：
   - 22（SSH，建议限自己的 IP）
   - 80 / 443（前端 + HTTPS）
   - ❌ 3306(MySQL)、6379(Redis)、48080(后端) **不要对公网开放**
3. **域名（可选但推荐）**：备案后解析到 ECS 公网 IP；SSL 用阿里云免费证书或 Let's Encrypt
4. 数据库建议直接用**阿里云 RDS MySQL** + **云数据库 Redis**（免运维、自动备份）；省钱方案也可 ECS 自装

## 阶段 1 · 环境安装（ECS 上）

```bash
# JDK 17
yum install -y java-17-openjdk java-17-openjdk-devel   # 或 tar 包安装
# MySQL 8（自装方案） / Redis（自装方案）
# nginx
yum install -y nginx
# 上传目录
mkdir -p /data/enterprise/files
```

## 阶段 2 · 数据迁移

```bash
# 本地导出（结构 + 种子数据；去掉 smoke_test 残留的业务测试数据）
# 密码不落盘：-p 交互式输入，或 --defaults-extra-file=<凭据文件>
mysqldump -uroot -p --default-character-set=utf8mb4 enterprise-pro > enterprise-pro.sql
# 云上导入（RDS 用 DMS 控制台或 mysql 客户端）
mysql -h <rds地址> -u<用户> -p enterprise-pro < enterprise-pro.sql
```

⚠️ 迁移后必改三处（连接的是云上库执行）：
1. `infra_file_config` id=29 本地存储：`basePath` 改 `/data/enterprise/files`（域名改线上地址）
2. 本地已上传的发票/附件文件：`scp D:\enterprise-pro-files/* root@服务器:/data/enterprise/files/`
3. `system_users` 的 admin 密码确认、演示用户已停用 ✓

## 阶段 3 · 后端上线

1. 新建 `application-pro.yaml`（参考 application-local.yaml）：
   - 数据源 → RDS 地址；密码用**环境变量注入**（不要明文进 git）
   - Redis → 云 Redis；`enterprise.captcha.enable: true`（上线开启验证码；注意是嵌套键，`captcha-enable` 是错误写法）
   - 文件 basePath → `/data/enterprise/files`
2. 本地打包上传：
   ```bash
   mvn package -DskipTests
   scp enterprise-server/target/enterprise-server.jar root@<ECS>:/data/app/
   ```
3. systemd 服务（`/etc/systemd/system/enterprise.service`）：
   ```ini
   [Service]
   # 密钥不再写在配置文件中，改由环境变量注入；文件权限 600，内容为 KEY=VALUE 逐行
   EnvironmentFile=/data/app/enterprise.env
   ExecStart=/usr/bin/java -Dfile.encoding=UTF-8 -jar /data/app/enterprise-server.jar --spring.profiles.active=pro
   Restart=always
   [Install]
   WantedBy=multi-user.target
   ```
   `/data/app/enterprise.env` 必填项（缺失会导致启动失败，这是刻意的快速失败）：
   ```
   ENTERPRISE_PRO_SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_PASSWORD=xxx
   ENTERPRISE_PRO_SPRING_DATASOURCE_DYNAMIC_DATASOURCE_SLAVE_PASSWORD=xxx
   ENTERPRISE_PRO_SPRING_DATA_REDIS_PASSWORD=xxx
   ```
   可选：`ENTERPRISE_PRO_WX_MP_SECRET`、`ENTERPRISE_PRO_WX_MINIAPP_SECRET`（未启用社交登录可留空）。
   `chmod 600 /data/app/enterprise.env && systemctl daemon-reload && systemctl enable --now enterprise`
4. 冒烟：`curl http://localhost:48080/admin-api/system/auth/login ...`

## 阶段 4 · 前端上线

1. 本地构建：
   ```bash
   # .env.prod（无 .env.production 文件）：VITE_BASE_URL='' 即同源，由 nginx 代理 /admin-api
   npm run build:prod   # 产物 dist-prod/（package.json 无裸 build 脚本）
   scp -r dist-prod/* root@<ECS>:/data/enterprise/front/
   ```
2. nginx 配置：直接使用仓库内 `deploy-backup/enterprise.conf` 整文件（**不要用同目录的
   `nginx-enterprise.conf`——那是 09-07 前的过期版本，缺 `/infra/ws` WebSocket 升级头与
   index.html no-cache，照抄会复发 IM 推送失效与发版后白屏**）：
   ```bash
   scp deploy-backup/enterprise.conf root@<ECS>:/etc/nginx/conf.d/enterprise.conf
   ssh root@<ECS> 'nginx -t && systemctl reload nginx'
   ```
   该文件已含：SPA try_files、`/admin-api/` 代理 48080、`/infra/ws` 升级头、
   index.html no-cache + /assets/ immutable、`/m/` 移动工作台子路径、gzip、client_max_body_size 20m。
3. 浏览器访问 `http://ECS_IP` → 登录 → 逐页验证

## 阶段 5 · 边测试边开发的迭代闭环

```
本地开发 → git commit/push (GitHub testrepository) → 服务器拉取或本地 scp → 重启 → 云上回归
```
- 简单版：服务器上写个 `/data/app/deploy.sh`（拉 jar + 重启 systemd）
- 进阶版：GitHub Actions push 到 main 自动 ssh 部署（后续可加）
- 测试数据：云上库定期重置种子（`sql/mysql/enterprise-biz.sql` 里业务表 TRUNCATE 段可复用）
- **数据库结构增量**：每次功能改动产生的 ALTER/INSERT（如 correction.sql/overtime.sql）
  在 CHANGELOG 里登记，云上按序补执行

## 阶段 5.1 · 改动同步与备份规矩（2026-09-07 实装，必须遵守）

1. **代码同步**：本地仓库是唯一事实源。任何改动先在本地改 → git 提交 → 再构建部署服务器
   （前端 `npm run build:prod` → dist-prod.tar.gz → scp → 解压 /data/enterprise/front；
   后端 `mvn package` → scp jar → `systemctl restart enterprise`，启动约 2.5 分钟）。
   **严禁直接在服务器上改代码/页面**，服务器只是运行环境。
2. **服务器配置备份**：`deploy-backup/` 保存了 application-pro.yaml / **enterprise.conf（当前生效版，含 /infra/ws 与 no-cache）** /
   enterprise.service / db.sh 的副本，服务器配置改动后要回拷到这里更新
   （application-pro.yaml 含密码，已在 .gitignore，不进 git；`nginx-enterprise.conf` 为历史遗留过期文件，勿再引用）。
3. **数据库每日备份**：服务器 cron `30 2 * * * /data/backup/db.sh`，gzip 备份到
   /data/backup/，保留 14 天。恢复命令：
   `gunzip < enterprise-pro-YYYY-MM-DD.sql.gz | mysql -uroot -p enterprise-pro`
4. **结构变更同步**：给云上库执行 DDL/字典/菜单 INSERT 前，先把 SQL 存入 `sql/mysql/`
   并登记 CHANGELOG，本地库先验证再上云。

## 上线前检查清单

- [ ] 验证码开启、数据库/Redis 密码非明文或环境变量
- [ ] `/data/app/enterprise.env` 已创建且权限 600（`application-pro.yaml` 已无明文密码，缺变量会启动失败）
- [ ] 已执行 `sql/mysql/fms_voucher_source_unique.sql`（幂等；先确认其输出的重复来源检查为空）
- [ ] 安全组未开放 3306/6379/48080
- [ ] swagger/knife4j 生产关闭（`springdoc.api-docs.enabled=false`）
- [ ] 日志轮转（logback 已配）+ 云监控告警（CPU/内存/磁盘）
- [ ] MySQL 自动备份策略开启（RDS 默认有）
- [ ] 文件目录磁盘配额（发票图片会持续增长）
