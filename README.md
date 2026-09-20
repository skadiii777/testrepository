# 企业管理系统（enterprise-pro）

面向中小企业的综合管理平台，覆盖**客户合同产品（CRM）**、**进销存**、**人事考勤**、**财务（FMS）**、**仓储（WMS）**、**即时通讯（IM）**等业务域，并提供员工自助工作台、统一审批中心与移动工作台（`enterprise-pro-app`，部署于 `/m/`）。

- 后端：Spring Boot 3.5 + Spring Security(OAuth2 Token) + MyBatis Plus + Redis，多模块架构（基于 yudao/ruoyi-vue-pro 脚手架深度定制）
- 前端：Vue3 + Element Plus + Vite（基于 yudao-ui-admin-vue3）

## 业务模块（enterprise-module-biz）

| 业务域 | 实体 |
| --- | --- |
| 客户合同产品 | 客户（含信用额度，确认销售时拦截超额）、产品、合同、供应商、线索、商机与跟进 |
| 进销存 | 采购单（入库自动加库存 + 移动加权成本）、销售单（出库自动扣库存 + 不足拦截）、库存（含在途库存）、库存流水（只增不改）、库存盘点、退货管理 |
| 人事考勤 | 员工、考勤（打卡迟到/早退判定）、请假（审批/销假）、假期余额（审批扣减/销假返还，原子防超扣）、部门角色映射 |
| 协作审批 | 业务汇报、费用报销（通过自动生成凭证）、补卡申请（通过自动回写考勤）、注册审批 |
| 财务 FMS | 会计科目（标准科目表）、记账凭证（借贷必平、草稿/已记账）、科目余额、财务报表（利润表/资产负债表/恒等式校验）、收付款管理 |
| 仓储 WMS | 库位、库位库存（主库存的分配视图）、上架/下架/移库（守卫式扣减）、单据驱动作业任务（采购入库→上架任务，销售出库→拣货任务，FIFO 消耗） |
| 即时通讯 IM | 好友、私聊消息（发送/增量拉取/已读）、WebSocket 实时推送 |
| 工作流 | 请假/报销/补卡三大审批接入 Flowable（BPM 模块），提交自动发起流程，未部署时降级本地直批 |
| 聚合 | 数据看板（ECharts）、审批中心（请假/报销/补卡三合一待办）、业绩目标（同员同月唯一 + 达成率）、公告与到期提醒 |

## 快速开始

1. **环境**：JDK 17、MySQL 8、Redis、Node 18+
2. **数据库**：创建 MySQL 空库，设置 `BIZ_DB_HOST/PORT/NAME/USER/PASSWORD`，安装 `tools/requirements-dev.txt` 后执行 `python tools/migrate.py --apply`。已有数据库先运行不带 `--apply` 的预检查，处理历史名称映射；详细步骤见[升级说明](docs/HARDENING-2026-09-11.md)。新入口已归并表结构，不再逐个导入旧 SQL。
3. **后端**：配置数据源地址，参照 `application-private.example.properties` 建立 Git 忽略的 `application-private.properties`。在项目根目录执行 `mvn package -DskipTests` → `java -jar enterprise-server/target/enterprise-server.jar`（端口 48080）。
4. **前端**：`enterprise-pro-ui` 目录 `npm install --legacy-peer-deps` → `npm run dev`（默认 80 端口，代理 `/admin-api` → 48080）
5. **首次管理员**：空库迁移后设置自己的 `BIZ_BOOTSTRAP_ADMIN_PASSWORD`（至少 12 字符），运行 `python tools/bootstrap_admin.py`。使用 admin 和所设密码登录（租户：企业平台）；已有用户的库不执行初始化。

**登录凭据按场景区分**：
- 空库首次启动：admin + 你在 `BIZ_BOOTSTRAP_ADMIN_PASSWORD` 所设的密码（见上一步）
- 已初始化的本地开发环境：admin / 本地开发密码（租户 id=1，header `tenant-id: 1`）
- 生产环境：非默认密码，凭据见服务器 `/data/app/`（600 权限），不写入任何仓库文件

## 自动化测试

```powershell
# 使用本地开发配置连接 MySQL，自动创建独立 QA 库并运行真实事务并发测试
python tools/test_biz.py --local-config

# 使用 BIZ_DB_* 连接配置，创建独立 QA 库验证旧库升级
python tests/test_migrations.py
```

原有 HTTP 冒烟脚本需要运行中的测试服务及 `BIZ_TEST_ADMIN_PASSWORD` 等环境变量，包含历史清理逻辑；升级后不要对业务库运行。并发测试不依赖已启动的后端、Redis 或正式业务数据。

## 文档

- [第二、三轮加固与升级说明](docs/HARDENING-2026-09-11.md) 实现变化、迁移、配置与验证结果
- [MIGRATION-STATUS.md](MIGRATION-STATUS.md) 迁移进度与环境速查
- [CHANGELOG.md](CHANGELOG.md) 更新日志
