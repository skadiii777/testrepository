# 更新日志（CHANGELOG）

## 2026-09-08 · 管理员/普通用户权限分离 + 登录弹窗修复

- **普通角色（common）收窄为纯员工视图**：回收 yudao 基础种子误授的系统管理/
  基础设施/流程管理/监控中心等全部管理端菜单，仅保留员工工作台 11 项授权
  （1 目录 + 5 页面 + 5 按钮）。服务器已生效；新增维护脚本
  `sql/mysql/common_role_reset.sql`（幂等，全新部署后在基础库导入后执行）
- **修复登录后"没有该操作权限"弹窗**：首页企业概览此前对无权限用户仍请求
  `/biz/dashboard/panel`（403 弹 toast）。现按 `biz:dashboard:query` 权限门控，
  无权限不请求；快捷入口同步按权限过滤（普通用户自动隐藏审批中心入口）
- 双角色浏览器实测：普通用户登录无弹窗、菜单仅首页+员工工作台、工作台页面
  全部可用；管理员菜单与功能不受影响

## 2026-09-07（晚）· 普通用户权限修复 + 注册自动分配角色

- **修复：自注册用户登录后无菜单/全部 403**。两层根因：
  1. 注册流程不分配任何角色 → AdminAuthServiceImpl.register 增加
     `assignDefaultRoleQuietly`：自动绑定启用状态的「普通角色」（code=common），
     失败仅告警不阻断注册
  2. 种子 SQL 的 system_role_menu 授权行 **tenant_id 写成 0**（INSERT 漏 tenant_id 列），
     权限校验按租户过滤后查不到授权 → 403。超管走免检通道故从未暴露。
     两库数据已修正（tenant_id 0→1），源文件 enterprise-biz.sql / correction.sql 已补列
- **修复：普通角色缺「员工工作台」目录菜单授权**（目录无授权整树不显示）。
  服务器已按 API 补授（role 2 + 菜单 12733），种子文件同步补充目录授权语句
- 顺带：本地发现 OA 演示页引用不存在的 @/views/oa/utils/constants（上游同样缺失），
  已整删 src/views/oa；前端 .env 默认登录租户改「企业平台」；首页重写为业务工作台
  （详见上一条目）

## 2026-09-07 · 回归复核 + 日志规范化

- 修复复核：补卡 BPM 审批通过后考勤未回写的问题确认已于上一批次修复
  （根因是 BPM 状态回调 Integer vs String 的 equals 类型错误，见 267b935）；
  重建后端重跑 smoke_test **164/164 全过**，日志确认监听器在 HTTP 线程同步执行、租户上下文正常
- 清理调试遗留：监听器/回写路径的 System.out.println 换成规范 log，
  回写失败会带完整堆栈进日志文件（之前只 printStackTrace 到控制台）

## 2026-09-06 · BPM 工作流（Flowable）

### 新增（测试 136 → 164 项全过）

- **移植 yudao-module-bpm**（包名 com.enterprise 化，243 个 Java 文件），
  Flowable 引擎首次启动自动建 45 张 ACT_/FLW_ 表
- **请假/报销/补卡三大审批接入 Flowable**：提交时自动发起流程（未部署时降级本地直批），
  BPM 状态监听器回写业务状态；请假通过扣假期余额、补卡通过自动回写考勤并重算迟到/早退
- 新增 BPM 业务表 `sql/mysql/bpm_tables.sql`（8 张）；
  biz_attendance_correction 加 process_instance_id 列
- 修复：BPM 状态回调 `"1".equals(Integer status)` 恒 false，导致补卡审批通过后考勤未回写
- 流程模型部署注意：type=10（BPMN 设计器）才会保存 bpmnXml，type=20（SIMPLE）会忽略

## 2026-09-05（晚）· 功能补全批次

### 新增（5 项，测试 111 → 131 项全过）

- **补卡申请**：工作台提交 → 审批中心处理 → 通过自动回写考勤并重算迟到/早退；
  SQL 层防重复审批；新表 biz_attendance_correction + 字典 biz_correction_*
- **加班时长**：下班打卡/补下班卡晚于 18:00 自动累计分钟（biz_attendance.overtime_minutes）；
  新页面「考勤月报」按员工汇总出勤/迟到/早退/缺勤/加班；工作台显示本月加班
- **单据状态机**：采购/销售单 0草稿→1已确认→2已完成，0/1 可作废为 3；
  库存联动从建单移至"完成"流转；完成失败事务回滚；状态只能经流转接口变更
  （新字典 biz_order_status，存量数据已迁移）
- **客户跟进记录**：新表 biz_customer_followup + 管理页 + 客户页内嵌跟进时间线弹窗
  （时间倒序 + 快捷新增），字典 biz_followup_method
- **周报摘要**：BizWeeklyDigestJob（infra_job 每周一 09:00）聚合上周新增客户/销售/采购/
  待审批数据，经站内信模板 biz_weekly_digest 推送管理员；看板支持手动触发
- 管理端新权限：biz:correction:* / biz:attendance:summary(复用query) /
  biz:{purchase,sales}:{confirm,complete,void} / biz:followup:* / portal:correction:*

### 修复

- 站内信模板 params 列需为 JSON 数组格式（JacksonTypeHandler），种子数据已修正

## 2026-09-05

### 首个版本（pro 分支）

**架构迁移**：从 enterprise-ms（RuoYi 4.8.3 / Thymeleaf 单体）整体迁移到 yudao 架构
（Spring Boot 3.5 + MyBatis Plus + Spring Security + Vue3 + Element Plus），功能全量保留。

- 后端新模块 `enterprise-module-biz`：14 个业务实体（yudao 规范：DO/Mapper/Service/Controller/VO）
  + 数据看板、审批中心、员工工作台三个聚合控制器
- 业务规则迁移：采购入库加库存、销售出库扣库存（不足拦截 + 事务回滚）、
  库存流水只增不改、请假审批扣余额/销假返余额（原子防超扣）、打卡迟到早退判定
- 多租户适配（TenantBaseDO + tenant_id）、逻辑删除（deleted）、审计字段（creator/updater）
- 前端 24 个文件：14 个 CRUD 管理页 + 数据看板（ECharts）+ 审批中心（三合一待办）+ 4 个工作台门户页
- 自动化测试 `tests/smoke_test.py`：**111 项全部通过**
- 去品牌化：标题/登录页外链/DocAlert(297 处)/logo/后端主类/数据库租户与用户名全部改为企业平台标识
- 精简侧边栏：停用未启用模块（商城/AI/CMS/OA 等）的 1863 条演示菜单
- 新增功能：补卡申请（提交 → 审批 → 自动回写考勤并重算迟到/早退状态）

### 已知事项

- 登录验证码默认关闭（自动化测试需要），生产开启 `enterprise.captcha-enable`
- `application-druid.yaml` 数据源为明文密码，生产建议环境变量注入
- 生成器 `tools/gen_biz_yudao.py` / `tools/gen_front.py` 重跑会覆盖生成目录，手工改动需同步模板
