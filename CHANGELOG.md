# 更新日志（CHANGELOG）

## 2026-09-09（续 2）· 客户联系人（多决策人，对齐 yudao CRM 联系人简化版）

### 新增（测试 242 → 253 项全过，本地+云上双认证）

- **客户联系人 biz_contact**（客户合同产品 → 联系人管理）：一个客户挂多个
  联系人（姓名/职位/手机/邮箱/微信/备注），客户名称冗余自动填充；
  接口 /biz/contact/create|update|delete|get|page|list-by-customer|export-excel
  （biz:contact:query/create/update/delete）
- **线索转商机联动**：转化时自动把线索联系人落入客户联系人表（决策人档案起点）
- 部署：sql/mysql/contact.sql（表+菜单，挂客户合同产品目录下，管理员专属）
- 冒烟测试第 18 节 11 项断言（冗余填充、换客户校验、转化落联系人等）；
  浏览器实测 UI 创建（周总监/星辰科技演示数据保留云上）

## 2026-09-09（续）· 部门角色映射管理页 + 商机漏斗图

- **部门角色映射管理页**（系统管理 → 部门角色映射，biz:dept-role-map:manage）：
  把注册审批预留的「按部门/职位开放权限」架构补上 UI——选部门 → 维护默认角色
  （可多个，重复添加幂等），注册审批通过时优先按此分配角色、未配置回退普通角色；
  页面顶部说明该机制与未来扩展方式。菜单种子 sql/mysql/dept_role_map_menu.sql
  （本地+云库已执行）。**本批仅前端+菜单 SQL，无后端改动、无需重启服务**
- **商机页漏斗图**：ECharts 漏斗图（保持 6 阶段顺序、tooltip 含单数与金额）
  + 阶段卡片两栏布局
- 浏览器验证：漏斗图 canvas 挂载、映射页选择部门联动加载角色、空态提示正确

## 2026-09-09 · CRM 线索 + 商机（销售漏斗）+ 修复全站表单提交循环引用 bug

### 新增（测试 224 → 242 项全过，本地+云上双认证）

- **销售线索 biz_clue**（企业管理 → 线索管理）：名称/联系人/电话/来源/跟进状态/
  负责人（创建时自动取登录人）；状态 0待跟进→1跟进中→2已转化/3已无效（终态锁定）
- **线索转商机**（一键）：自动创建客户（按名称复用或新建，带出联系人/电话/来源）+
  创建商机（初始阶段默认初步接触），线索置已转化并记录客户 id；重复转化/终态编辑被拒
- **商机 biz_business**（企业管理 → 商机管理）：挂客户下，6 阶段销售漏斗
  （初步接触→需求确认→方案报价→谈判协商→赢单/输单终局，置终局不可再改）；
  **漏斗统计条**（funnel-stats：各阶段数量+预期金额）；一键标记赢单
- 部署：sql/mysql/crm.sql（两表+三字典+两菜单，管理员专属）
- 冒烟测试第 17 节 18 项断言（转化建客户/商机、终态锁、漏斗金额等）

### 修复

- **全站表单提交循环引用 bug（38 处）**：所有 biz 页面新建/编辑提交时把 Vue 3.5
  的 Ref 包装对象直接传给 axios，JSON.stringify 报 "Converting circular structure
  to JSON"——**所有管理页的创建/编辑按钮在 UI 上全部不可用**（冒烟测试走 API 未覆盖）。
  浏览器实测线索创建时发现，已批量修复为 formData.value 并重新部署
- 云上 242/242 回归 + 浏览器端到端验证：建线索→转商机→漏斗亮起（演示数据
  星辰科技有限公司已保留在云上作为示例）

## 2026-09-08（续 5）· 销售/采购退货管理（对齐 yudao ERP Return 单）

### 新增（测试 203 → 224 项全过，本地+云上双认证）

- **退货单 biz_return**（单表双类型，复用收付款管理模式）：销售退货
  （关联已完成销售单，执行后货物**入库**）/ 采购退货（关联已完成采购单，
  执行后货物**出库**退回供应商）；状态机 0待退货 → 1已退货 / 3已作废
- **业务规则**：仅已完成（状态2）单据可退货；同一原单累计退货数量
  （不含已作废）不可超过原单数量；退货单价默认取原单单价、总额服务端计算；
  执行退货联动库存（复用 StockService.changeStock，流水 sourceType=
  sales_return/purchase_return）；重复执行/编辑已退货单/作废后执行均被拒
- **接口**：/biz/return/create|update|delete|get|page|execute|void|
  returned-sum|export-excel（biz:return:query/create/update/delete）
- **退货管理页**（企业管理 → 退货管理，管理员专属）：类型/状态/日期筛选、
  新建弹窗联动已完成单据（自动带出对方/产品/单价，展示原单数量/已退/可退）、
  执行/作废/编辑/删除、金额合计行、导出
- 部署：sql/mysql/return.sql（表+字典+菜单）
- 冒烟测试第 15 节：21 项断言（入库+5/出库-10 库存联动、超量拒绝、
  作废不计汇总等）；云上生产机跑通 224/224

## 2026-09-08（续 4）· 云上全量回归 + 注册页/审批页 UI 验证 + 品牌残留清理

- **云上全量回归**：smoke_test.py 在生产机直接对 127.0.0.1 跑通 **203/203**（生产环境认证）
- **UI 验证**：注册表单（部门/职位下拉正常加载选项）、注册审批页（列表/状态/驳回原因渲染正常）
- **品牌残留清理**：根部门「芋道源码」→「集团总部」（本地+云库已执行，
  `sql/mysql/rebrand_cleanup.sql` 幂等脚本，服务器留档 /data/enterprise/sql/）

## 2026-09-08（续 3）· 注册联动部门/职位 + 管理员注册审批 + 预留部门权限架构

### 新增（测试 188 → 203 项全过）

- **注册流程改造**：注册页选择申请部门 + 职位（/system/auth/register-options
  未登录可访问，仅含部门/岗位 id 与名称）；注册 = 提交待审批申请
  （新表 biz_register_apply，账号不落 system_users、不可登录），提交后提示等待审批
- **登录提示**：待审批账号登录时返回明确提示"等待管理员审批"（错误码 1_002_003_012），
  不再误报"账号密码不正确"
- **注册审批页**（系统管理 → 注册审批，biz:register-apply:*）：待审批列表 →
  通过（创建正式账号：启用 + 入申请部门/岗位 + 分配角色）/ 驳回（记录原因）；
  通过弹窗可改分配角色，留空则按部门映射/普通角色
- **预留架构（按部门/职位开放模块权限的挂载点）**：新表 biz_dept_role_map +
  接口 /system/dept-role-map/list-by-dept|create|delete；审批通过时优先按部门映射
  分配角色，无映射回退「普通角色」。未来新模块上线，按部门/职位维护映射即可批量开放
- 部署：sql/mysql/register_apply.sql（两表+菜单，显式 tenant_id=1）
- 单测适配：AdminAuthServiceImplTest.testRegister_success 适配新签名

## 2026-09-08（续 2）· 修复管理员审批按钮不可见 + 普通用户开放客户合同产品

### 修复

- **管理员/普通用户所有业务审批按钮在界面中被隐藏**：前端 v-hasPermi 引用的
  `biz:approval:audit` 等 5 个权限串从未落菜单表（biz:approval:audit、
  biz:correction:create/update、biz:followup:update、biz:stockcheck:create），
  指令精确匹配失败直接把按钮从 DOM 移除。已补齐按钮菜单（两库 +
  `sql/mysql/fix_missing_perms.sql` 幂等脚本），并同步修补 stock_check.sql 种子
- **超管通配兜底**：get-permission-info 对超管角色下发 `*:*:*` 权限串
  （AuthController），与前端指令的通配协议对齐，杜绝"权限串漏配→超管按钮消失"整类问题
- 服务端 API 本就超管免检（冒烟测试全绿而界面不可用的根因）

### 调整

- **普通角色（common）开放客户合同产品子树**（31 项：目录+客户/产品/合同/供应商/
  客户跟进页面及增删改导按钮，另补授「企业管理」父目录——父目录缺授权时整棵子树
  不会渲染）。普通角色现共 43 项授权；common_role_reset.sql 已同步
- 双角色浏览器实测：admin 审批按钮可见并完成一次真实审批（待办 8→7）；
  testuser02 可见客户合同产品全部页面（24 条客户数据、新增按钮可用）

## 2026-09-08（续）· 新增库存盘点（对齐 yudao ERP StockCheck）

### 新增（测试 177 → 188 项全过）

- **库存盘点单**：新表 biz_stock_check + 管理页（企业管理 → 库存盘点，biz:stockcheck:*）。
  发起盘点（下拉选择库存产品、自动显示当前账面）→ 确认时按实盘调整库存并写库存流水
  （sourceType=stockcheck，盘盈入库/盘亏出库沿用量值语义），差异自动计算（实盘-账面）
- **确认时重新快照账面**：创建后库存可能变动（采购入库/销售出库），确认以当下库存为账面，
  防止快照过期导致错调；差异数按确认时点计算
- 已确认盘点单不可删除/重复确认（保护库存轨迹）；仅待确认可删除
- 部署：sql/mysql/stock_check.sql（表+字典+菜单，显式 tenant_id=1）

## 2026-09-08 · 新增收付款管理（对齐 yudao ERP 收付款单 / CRM 回款）

### 新增（测试 164 → 177 项全过）

- **收付款流水**：新表 biz_payment + 管理页（企业管理 → 收付款管理，biz:payment:*）；
  销售单（已完成）登记收款、采购单登记付款；单号 SK/FK+时间戳；支持分页/日期区间/
  单据编号/对方名称筛选、金额合计行、Excel 导出
- **金额闭环校验**：仅已完成（状态 2）单据可登记；收付类型与单据类型强制匹配
  （收款↔销售、付款↔采购）；累计收付金额不得超单据总额
- **看板**：panel 接口新增本月收款（monthReceived）/本月付款（monthPaid）
- **关联修复**：biz_sales/biz_purchase 的 total_amount 自迁移起从未计算（恒 NULL），
  创建单据时现按 数量×单价 落库；收付款校验对历史空值行做 兼容回退
- 部署：sql/mysql/payment.sql（表+字典+菜单，均显式 tenant_id=1 规避种子租户坑）

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
