# 阿里云部署指南（enterprise-pro 上线步骤）

> 目标：把「后端 48080 + 前端静态页 + MySQL + Redis」落到阿里云 ECS，边测试边开发。

## 阶段 0 · 服务器与账号准备

1. **ECS**：2核4G 起步（开发测试够用），系统选 Alibaba Cloud Linux 3 或 Ubuntu 22.04
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
mysqldump -uroot -p123456 enterprise-pro --default-character-set=utf8mb4 > enterprise-pro.sql
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
   - Redis → 云 Redis；`captcha-enabled: true`（上线开启验证码）
   - 文件 basePath → `/data/enterprise/files`
2. 本地打包上传：
   ```bash
   mvn package -DskipTests
   scp enterprise-server/target/enterprise-server.jar root@<ECS>:/data/app/
   ```
3. systemd 服务（`/etc/systemd/system/enterprise.service`）：
   ```ini
   [Service]
   Environment=MYSQL_PASS=xxx REDIS_PASS=xxx
   ExecStart=/usr/bin/java -Dfile.encoding=UTF-8 -jar /data/app/enterprise-server.jar --spring.profiles.active=pro
   Restart=always
   [Install]
   WantedBy=multi-user.target
   ```
   `systemctl daemon-reload && systemctl enable --now enterprise`
4. 冒烟：`curl http://localhost:48080/admin-api/system/auth/login ...`

## 阶段 4 · 前端上线

1. 本地构建：
   ```bash
   # .env.production：VITE_BASE_URL='https://你的域名'（或 http://ECS IP）
   npm run build        # 产物 dist/
   scp -r dist/* root@<ECS>:/data/enterprise/front/
   ```
2. nginx 配置（`/etc/nginx/conf.d/enterprise.conf`）：
   ```nginx
   server {
     listen 80;
     root /data/enterprise/front;
     location / { try_files $uri $uri/ /index.html; }        # SPA 路由
     location /admin-api/ { proxy_pass http://127.0.0.1:48080; }
     client_max_body_size 20m;                                # 发票图片上传
   }
   ```
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
2. **服务器配置备份**：`deploy-backup/` 保存了 application-pro.yaml / nginx-enterprise.conf /
   enterprise.service / db.sh 的副本，服务器配置改动后要回拷到这里更新
   （application-pro.yaml 含密码，已在 .gitignore，不进 git）。
3. **数据库每日备份**：服务器 cron `30 2 * * * /data/backup/db.sh`，gzip 备份到
   /data/backup/，保留 14 天。恢复命令：
   `gunzip < enterprise-pro-YYYY-MM-DD.sql.gz | mysql -uroot -p enterprise-pro`
4. **结构变更同步**：给云上库执行 DDL/字典/菜单 INSERT 前，先把 SQL 存入 `sql/mysql/`
   并登记 CHANGELOG，本地库先验证再上云。

## 上线前检查清单

- [ ] 验证码开启、数据库/Redis 密码非明文或环境变量
- [ ] 安全组未开放 3306/6379/48080
- [ ] swagger/knife4j 生产关闭（`springdoc.api-docs.enabled=false`）
- [ ] 日志轮转（logback 已配）+ 云监控告警（CPU/内存/磁盘）
- [ ] MySQL 自动备份策略开启（RDS 默认有）
- [ ] 文件目录磁盘配额（发票图片会持续增长）
