# 企业管理系统（enterprise-pro）

面向中小企业的综合管理平台，覆盖**客户合同产品（CRM）**、**进销存**、**人事考勤**三大业务域，并提供员工自助工作台与统一审批中心。

- 后端：Spring Boot 3.5 + Spring Security(OAuth2 Token) + MyBatis Plus + Redis，多模块架构（基于 yudao/ruoyi-vue-pro 脚手架深度定制）
- 前端：Vue3 + Element Plus + Vite（基于 yudao-ui-admin-vue3）

## 业务模块（enterprise-module-biz）

| 业务域 | 实体 |
| --- | --- |
| 客户合同产品 | 客户、产品、合同、供应商 |
| 进销存 | 采购单（入库自动加库存）、销售单（出库自动扣库存 + 不足拦截）、库存、库存流水（只增不改） |
| 人事考勤 | 员工、考勤（打卡迟到/早退判定）、请假（审批/销假）、假期余额（审批扣减/销假返还，原子防超扣） |
| 协作审批 | 业务汇报、费用报销、补卡申请（通过自动回写考勤） |
| 聚合 | 数据看板（ECharts）、审批中心（请假/报销/补卡三合一待办） |

## 快速开始

1. **环境**：JDK 17、MySQL 8、Redis、Node 18+
2. **数据库**：创建库 `enterprise-pro`，依次导入
   - `sql/mysql/ruoyi-vue-pro.sql`（基础库）
   - `sql/mysql/quartz.sql`
   - `sql/mysql/enterprise-biz.sql`（业务表/字典/菜单）
   - `sql/mysql/correction.sql`（补卡申请）
3. **后端**：改 `enterprise-server/src/main/resources/application-local.yaml` 数据源 → `mvn package -DskipTests` → `java -jar enterprise-server/target/enterprise-server.jar`（端口 48080）
4. **前端**：`enterprise-pro-ui` 目录 `npm install --legacy-peer-deps` → `npm run dev`（默认 80 端口，代理 `/admin-api` → 48080）
5. **登录**：admin / admin123（租户：企业平台）

## 自动化测试

```bash
# 前提：后端已启动，验证码已关闭
python tests/smoke_test.py   # 111 项：鉴权/CRUD/业务规则/看板/预警/导出/门户/余额/流水/报销/补卡
```

## 文档

- [MIGRATION-STATUS.md](MIGRATION-STATUS.md) 迁移进度与环境速查
- [CHANGELOG.md](CHANGELOG.md) 更新日志
