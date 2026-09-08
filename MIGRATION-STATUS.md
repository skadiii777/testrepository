# enterprise-pro 迁移进度清单（2026-09-04 收工状态）

> 目标：把 enterprise-ms（RuoYi 4.8.3 / Thymeleaf）的全部业务功能迁移到 yudao 架构
> （Spring Boot 3.5 + MyBatis Plus + Spring Security + Vue3），全新项目，功能保留。

## ✅ 已完成（今天）

1. **脚手架**：`E:\AI-Code\enterprise-pro`，从 ruoyi-vue-pro master-jdk17 复制精简
   - 模块：enterprise-dependencies / framework / server / module-system / module-infra / **module-biz（新）**
   - 全部重命名：`cn.iocoder.yudao` → `com.enterprise`（包名+目录+配置键+groupId）
2. **编译修复（3 个坑，已解决）**
   - 全局 `~/.m2/settings.xml` 有 `jdk-1.8` profile 强制 source=1.8
     → 根 pom maven-compiler-plugin 显式 `<release>17</release>`（勿动全局配置）
   - spring-boot BOM 里 `maven.compiler.source=1.8`
     → enterprise-dependencies/pom.xml 显式覆盖 17
   - 验证码 SPI 文件 `META-INF/services/*Captcha*` 引用旧包名 → 已改
3. **数据库**：MySQL 库 `enterprise-pro`（root/123456），导入 yudao 基础 SQL（59 张表）
   - 数据源已指向该库（application-local.yaml），Redis 本机 6379
4. **服务启动验证 ✓**：`enterprise-server.jar` 启动成功（端口 **48080**，21 秒）
   - 登录接口 OK：`POST /admin-api/system/auth/login`（admin/admin123，header `tenant-id: 1`）
5. **业务模块 enterprise-module-biz 生成完毕并 BUILD SUCCESS**（116 个 Java 文件）
   - 14 实体：customer/product/contract/supplier/purchase/sales/stock/stockmove/
     employee/attendance/leave/quota(=LeaveQuota)/report/expense
   - 每实体：DO(TenantBaseDO) + Mapper(BaseMapperX+LambdaQueryWrapperX) + 3 VO +
     Controller(CommonResult/@PreAuthorize/Excel导出) + Service
   - 特殊：DashboardController（panel/trend/productTop/status）、ApprovalController
     （pending/leave-page/leave-audit/expense-page/expense-audit）、PortalController
     （打卡/我的请假+销假/汇报/报销 全套自助接口）
   - 业务规则已迁：采购入库加库存、销售出库扣库存+不足拦截(@Transactional)、
     流水只增不改、请假审批扣余额、销假返余额、配额唯一键+原子扣减、打卡迟到早退判定
   - 生成器：`tools/gen_biz_yudao.py`（可清空重建 biz src，可反复跑）

## ✅ 2026-09-05 追加完成

7. **业务 SQL 已导入**（14 表 + 10 字典 + 98 菜单权限）
8. **服务重启验证通过**：/biz/customer/page、/biz/dashboard/panel、
   /biz/approval/pending、/portal/index-data 全部 code=0
9. **smoke_test.py 迁移版完成：99/99 全部通过**
   - 覆盖：鉴权、8 模块 CRUD、采购/销售/流水规则链、看板、预警闭环、
     3 模块导出、门户打卡/请假/销假/汇报、余额闭环、报销+审批中心
   - 过程修复：punch-out 迟到状态未设 id（生成器已修）、
     LeaveQuotaMapper.adjustUsedDays 的 <script> 包裹、
     leave/expense empName 改为服务端填充（非必填）
   - 注意：导出接口响应无 Content-Type 头，测试用 PK 魔数（b'PK'）断言

## ✅ 2026-09-05 前端完成（下午）

- 前端仓库：`E:\AI-Code\enterprise-pro-ui`（yudao-ui-admin-vue3 element-plus 版，gitee 克隆）
- 已生成/编写 24 个文件（生成器 `tools/gen_front.py` + 模板 `tools/front_index_template.vue.tpl`）：
  - `src/api/biz/{14模块}/index.ts` + `src/api/biz/dashboard/index.ts`（含审批中心 API）+ `src/api/portal/index.ts`
  - `src/views/biz/{14模块}/index.vue`（搜索/表格/分页/弹窗表单，mes 样例三段式）
  - `src/views/biz/dashboard/index.vue`（ECharts 看板）、`src/views/biz/approval/index.vue`（双 Tab 审批）
  - `src/views/portal/{index,leave,report,expense}/index.vue`（打卡/请假销假/汇报/报销）
  - `src/utils/dict.ts` 注册 10 个 BIZ_* 字典类型
- `.env.local` 已确认 VITE_BASE_URL=http://localhost:48080、VITE_API_URL=/admin-api
- npm install 已完成（npmmirror 源，需 `--legacy-peer-deps`，video.js peer 冲突）
- **vue-tsc 类型检查全绿**；vite 依赖扫描 0 错误（修复：dict.ts 枚举尾逗号、
  生成器 qfields/rules 尾逗号）
- **开发服务器已启动：http://localhost:80**（vite ready 3s），
  后端 48080 在线，登录 admin/admin123 + 租户 1，菜单驱动加载 biz/portal 页面

## ⏳ 剩余待办

1. **浏览器逐页验证**：登录 → 企业管理各菜单 → 员工工作台 → 看板（数据均为空库初始态）
2. **README.md + CHANGELOG.md**（企业平台视角重写）
3. 旧项目 enterprise-ms 保留不动，作为业务规则参考与数据对照

## ⚠️ 关键 gotcha（接手必读）

- 编译命令必须带 `-am`：`mvn compile -pl enterprise-module-biz -am`（否则兄弟模块解析失败）
- java 用 `D:\develop\Program Files\Java\jdk-17`（系统 JAVA_HOME 是坏的 `%JAVA.8_HOME%`）
- yudao 服务端口 **48080**（不是 8090）；接口前缀 `/admin-api/**`
- DO 继承 TenantBaseDO → 表必须带 `tenant_id`（biz.sql 已含，默认 0/租户 1）
- 生成器重跑会清空 biz src 再生成；**手工修改业务代码要同步回生成器模板**，
  或者等生成器稳定后再手改
- 老项目遗留坏味道（迁移时已规避）：原 `biz_stock_move.move_id` 等主键命名已统一为 `id`
- enterprise-ms 的 sql/biz.sql 缺 v4 三表 DDL（quota/stock_move/expense）——
  新项目 enterprise-biz.sql 已补全，勿回抄旧文件

## 环境速查

| 项 | 值 |
|---|---|
| 新项目 | E:\AI-Code\enterprise-pro |
| 旧项目 | E:\AI-Code\enterprise-ms（保留参考） |
| yudao 原仓库 | E:\AI-Code\ruoyi-vue-pro（master-jdk17 分支） |
| Odoo 参考 | E:\AI-Code\odoo18 |
| JDK17 | D:\develop\Program Files\Java\jdk-17 |
| Maven | D:\apache-maven-3.9.4\bin\mvn.cmd |
| MySQL | localhost:3306 root/123456，库 enterprise-pro |
| Redis | localhost:6379（需手动启动 D:\redis-5.0.10） |
| 服务 | http://localhost:48080，登录 admin/admin123 |
| 旧服务 | http://localhost:8090（enterprise-ms，独立运行） |

## ✅ 已解决问题记录

**补卡 BPM 审批通过后考勤未回写**（2026-09-06 浏览器验收发现，当日修复于 267b935）：
- 根因：`updateCorrectionStatusFromBpm` 里用 `"1".equals(status)` 判断"通过"，
  而 BPM 回调的 status 是 Integer（2=APPROVE），恒 false → 回写考勤被跳过
- 修复：改用 `Integer.valueOf(2).equals(status)`（leave/expense 监听器同款写法本来就对）
- 验证：smoke_test 三段 BPM 端到端（请假扣余额/报销回写/补卡回写考勤）164/164 全过；
  2026-09-07 重建后端重跑仍 164/164，日志确认监听器在 HTTP 线程同步执行、租户上下文正常
  （当初"监听器线程丢租户上下文"和"insert 在 Flowable 事务里被回滚"两个假设均不成立）
- 教训：Integer/String equals 混用是 BPM 状态回调的固定坑，新增监听器一律用
  `Integer.valueOf(N).equals(status)` 写法

## ⚠️ 待观察

**前端点菜单会"偶尔刷新页面"**——初步判断是 keep-alive 首次挂载的正常数据加载 +
Element Plus 菜单组件的无害警告，待进一步确认是否有真实路由刷新。

## 前后端服务与 git

- 后端已推 GitHub main（267b935 补卡 BPM + 前一批功能补全）
- 前端 pro-ui 分支本地已提交 d0a2b7a，推送被网络拦，等窗口期重试：
  `git push github pro-ui`（remote github 已配置）
