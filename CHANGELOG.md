# 更新日志（CHANGELOG）

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
