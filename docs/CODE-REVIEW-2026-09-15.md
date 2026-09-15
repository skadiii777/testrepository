# enterprise-pro 代码与工程评审报告

> 评审日期：2026-09-15
> 评审范围：`E:\AI-Code\enterprise-pro`（后端）+ `E:\AI-Code\enterprise-pro-ui`（前端）
> 评审方式：通读全部文档 + 抽样精读 30+ 源文件 + git/构建/依赖实况核查
> 结论口径：所有问题均附**文件路径与行号**证据；未发现的项明确标注"未发现"，不做推测性指控

---

## 阶段一修复状态（2026-09-15 当日执行）

| 编号 | 问题 | 状态 | 验证方式 |
|---|---|---|---|
| P0-1 | 前端 `pro-ui` 无远程备份 | ✅ 已修复 | 已推送至自有仓库 |
| P0-1b | **后端 main 有 13 个提交未推送**（评审时遗漏，执行中发现） | ✅ 已修复 | 已推送至自有仓库 |
| P0-2 | FMS 凭证号 + 幂等无约束 | ✅ 已修复 | 编译通过；本地库实测约束生效、脚本幂等 |
| P0-3 | 生产配置明文密钥 | ⚠️ 部分修复 | 配置已改环境变量；**密钥轮换需在服务侧执行** |
| P0-4 | `tmp_biz.jar` 入库 + Dockerfile 失效 | ✅ 已修复 | 已移出索引；Dockerfile 已修正（无 docker 环境未构建验证） |
| P1-1 | 补卡 BPM 回调无事务 + 吞异常 | ✅ 已修复 | 编译通过 |
| P1-1b | **补卡监听器误传 businessKey**（评审时遗漏，修复中连带发现） | ✅ 已修复 | 编译通过 |

修复详情与部署影响见 [CHANGELOG.md](../CHANGELOG.md) 的 2026-09-15 条目。

---

## 阶段二进展（2026-09-15 当日）

### 已完成

| 项 | 状态 | 说明 |
|---|---|---|
| P1-3a CI 工作流 | ⚠️ 就绪待启用 | `docs/ci/github-actions-ci.yml`：build（编译+打包）+ secret-scan（明文密钥扫描）。两 job 命令与规则均本地实测通过 |
| P1-3b 密钥回归防护 | ✅ 规则就位 | 扫描规则当前 0 命中；用含明文样例反向验证确认有效 |

**CI 未启用的原因（非技术债，是凭据权限）**：本机 PAT 权限为 `X-OAuth-Scopes: repo`，
缺少 `workflow` scope，GitHub 拒绝推送 `.github/workflows/` 下的文件。
启用步骤见 `docs/ci/README.md`（补 scope 或改用 SSH，一条 `git mv` 即可）。

### 新增数据：前端 `ts:check` 现状

`npm run ts:check` → **49 条错误**（评审时为 50），分布：

| 错误码 | 数量 | 主要来源 |
|---|---|---|
| TS2307 | 17 | 全部为 `@/api/mall/*` 找不到模块 |
| TS6133 | 15 | 声明但未使用 |
| TS2339 | 9 | 属性不存在 |
| TS2304 | 4 | 找不到名称 |
| TS2367 | 2 | 比较疑似笔误 |
| TS2322 | 2 | 类型不可赋值 |

**17 条 TS2307 的根因已定位**：`src/components/DiyEditor/`（93 个文件 / 412K）是上游遗留的
移动端页面搭建器，其 `components/mobile/*` 引用了已被删除的 mall 模块。核查结论——**该目录为死代码**：

- `src/router` 中无任何 `diy` 路由
- 全 `src` 无 `<DiyEditor>` 模板使用，仅在**自动生成**的 `src/types/auto-components.d.ts` 中出现
- `build/vite/index.ts:63` 的自动导入 globs **已显式排除** `DiyEditor/components/mobile/**`
  （即作者已知其不参与构建）

处置二选一（属产品决策，未代为执行）：
- **删除** `src/components/DiyEditor/`：消掉 17 条错误并减少 412K 代码，与既有的上游模块瘦身方向一致
- **从 tsconfig 排除**：`exclude` 增加 `"src/components/DiyEditor"`，保留代码但不再类型检查

### 执行事故记录（诚实留档）

排查前端推送失败时，一次 `git rebase` 因超过工具超时被强杀，中断了 git 自动 gc，
导致 `enterprise-pro-ui` 的 `.git/refs`、`.git/logs` 与松散对象被清，**该仓库原 19 个本地提交
的历史不可恢复**（工作区源码无损，已重建并推送，生产构建验证通过）。

**根因链**（供后续参考）：
1. 前端仓库是浅克隆，`.git/shallow` 边界 `aab14fb` 是合并提交，其父提交不在本地
2. 推送到不含该上游历史的远端时 git 发瘦包、假定远端已有父提交 → 服务端
   `fatal: did not receive expected object ...` → `remote unpack failed: index-pack failed`
3. 这与网络无关（该次 13.39 MiB 以 120 KiB/s 完整传完）；`CONNECT tunnel failed 502` 是
   另一个独立的代理抽风问题（curl 抽样失败率约 50%）

**新增纪律**：在可能超时的前提下，不得运行会触发 gc 的写操作（`rebase`/`commit`/`gc`/`repack`），
必须加 `-c gc.auto=0` 或置于后台并给足时间。已在前端仓库设置 `gc.auto=0`。

---

## 一、项目概况

| 维度 | 实况 |
|---|---|
| 定位 | 中小企业综合管理平台：CRM + 进销存 + 人事考勤 + WMS + FMS + IM + AI |
| 后端 | Spring Boot 3.5.15 / JDK 17 / MyBatis Plus / Spring Security(OAuth2) / Flowable / Redis |
| 前端 | Vue3 + Element Plus + Vite（yudao-ui-admin-vue3 定制，独立仓库） |
| 架构 | Maven 多模块（dependencies / framework / server / system / infra / biz / bpm / im） |
| 后端规模 | 1876 个 Java 文件（自研核心 `enterprise-module-biz` 252 个） |
| 数据模型 | 154 张表（`sql/migrations/V001__schema.sql`） |
| 前端规模 | 36k 文件，自研业务页 27 个模块目录 + 5 个门户页 |
| 提交历史 | 34 次提交，工作区干净，单 main 分支 |
| 文档 | README / CHANGELOG(31KB) / MIGRATION-STATUS / DEPLOY-ALIYUN / docs/HARDENING / docs/RAG-LEARNING-GUIDE |

**演进路线**（从 git log 可完整还原）：
RuoYi 4.8.3 老系统 → yudao 架构迁移 → 业务规则补齐 → 上线阿里云 → 权限体系拆分 → 三轮外部审查加固 → 模块横向扩展（CRM→IM→FMS→WMS）→ 持续迭代。

---

## 二、文档评估

**这是本项目最被低估的资产。** 六份文档形成了一套自洽的工程档案：

| 文档 | 评价 |
|---|---|
| `README.md` | 结构清晰，5 步快速开始，明确标注环境变量与安全边界（管理员密码不得默认值） |
| `CHANGELOG.md` | 31KB，按日期+主题组织，每条含**实现要点 + SQL 文件名 + 验证结果**，可当作开发日志读 |
| `MIGRATION-STATUS.md` | 罕见地记录**失败假设与教训**（如"监听器线程丢租户上下文"被证伪、"Integer/String equals 混用"是固定坑） |
| `DEPLOY-ALIYUN.md` | 分阶段可执行，含安全组白名单、systemd 配置、nginx SPA 规则、回滚命令 |
| `docs/HARDENING-2026-09-11.md` | 最专业的一份：明确写出**验证边界**——"本轮未执行，不能沿用上一轮通过结论" |
| `docs/RAG-LEARNING-GUIDE.md` | 六步教学式讲解嵌入/检索/RAG/分块/Agent/选型，是给接手人的学习材料 |

**亮点**：`HARDENING` 文档结尾那张验证表，主动列出"前端 ts:check 未通过：50 条存量类型错误""Mimosa 门禁未完成重新扫描，不能宣称通过"。这种**拒绝粉饰**的文档习惯，比代码本身更值钱。

**不足**：文档只覆盖"做了什么"，缺"整体架构图/领域模型图/接口契约"，新人接手仍要靠读代码理解全局。

---

## 三、优点

### 1. 业务建模有真实深度，不是 CRUD 堆砌
远超一般脚手架改皮的水平，多处体现对业务的思考：
- **成本移动加权**：采购入库时 `新成本=(原库存×当前成本+本次入库金额)/(原库存+本次数量)`，出库自动结转
- **信用额度**：应收 = 已确认+已完成销售 − 净收款，确认销售单时拦截超额
- **在途库存**：已确认未完成的采购单按产品+仓库汇总
- **WMS 定位克制**：库位库存是主库存的"分配视图"，上架/下架只动库位表不动 `biz_stock`，零风险接入
- **FMS 赊购口径**：入库挂应付(2202)→付款冲应付，避免库存双重计入，科目余额可验会计恒等式

### 2. 并发正确性意识强（这是最容易翻车的地方）
- 状态流转全部 CAS：`WHERE status=旧值` + 检查影响行数，杜绝并发重复完成导致双倍扣库存
- 库存扣减在 SQL 层保证：`StockMapper.java:30-32` `quantity + delta >= 0`
- 收付款幂等：`requestId` 相同内容重试返回原流水，编号复用但内容不同则拒绝
- BPM 回调防重、请假审批 CAS、配额原子扣减
- 单号统一随机 + 数据库唯一索引兜底，历史重复编号保留 `legacy_document_no`

### 3. 迁移工程化程度高
不是手敲 SQL 改库，而是：`V001__schema.sql` 归并 154 表 → `tools/migrate.py` 版本表 + 校验和 + 数据库锁 → 预检查拦截歧义（重名产品/员工、重复余额）→ 映射文件人工确认 → `--apply` 执行。**生产发布记录**（CHANGELOG 2026-09-12）显示预检 176 项、生成 175 条映射后成功迁移。

### 4. 安全基线扎实（多项经核查确认无问题）
- **租户隔离**：33 个 DO 全部 `extends TenantBaseDO`，`ignore-tables` 为空，覆盖完整
- **金额精度**：全量 `BigDecimal`，无一处 double/float
- **SQL 注入**：无 Mapper XML，`@Select` 全用 `#{}`
- **异常处理**：无 `printStackTrace`、无空 catch
- **权限注解**：33 个 Controller 中 27 个 `@PreAuthorize` 覆盖完整（14/14、10/10 等）
- 共享 YAML 中 20 处字面量密码已改环境变量，私有配置 gitignore

### 5. 部署运维闭环完整
每日 2:30 自动备份（保留 14 天）+ 备份回拷留档 + nginx 缓存策略（index.html no-cache 根治发版白屏）+ WebSocket 升级头修复 + 明确回滚方案（jar + db 双回滚）。

### 6. 测试是"真"的
`BizConcurrencyTest.java` 是**真 MySQL + InnoDB 行锁 + 租户拦截器**的集成测试（仅 mock BPM），覆盖收款并发超额、请求重试、冲销、出库、请假/销假、BPM 重复回调、跨租户访问等 18 例。比大量"mock 一切"的伪测试有价值得多。

### 7. 生成器与手写代码分离
`tools/gen_biz_yudao.py` 可反复重跑，但**输出隔离到 `generated/<批次>` 目录**，保护手写实现不被覆盖——这是吃过亏之后的正确设计。

---

## 四、缺点与风险（分级）

### P0 · 严重（建议立即处理）

**P0-1 前端 `pro-ui` 分支只存在于本地，无任何远程备份**
- 证据：`git branch -vv` 显示 `* pro-ui 9f9e8e3`（**无上游跟踪**）；`origin` 指向上游 `gitee.com/yudaocode/yudao-ui-admin-vue3` 而非自有仓库；`MIGRATION-STATUS.md:115` 自述"推送被网络拦，等窗口期重试"
- 影响：27 个业务页面 + 5 个门户页 + 全部 API 封装，**单点故障**。硬盘故障即全部自研前端工作归零
- 修复：立即 `git push github pro-ui`，并把 `origin` 改指自有仓库

**P0-2 FMS 凭证号取值空间过小，且自动凭证幂等无唯一约束兜底** ✅ 已于 2026-09-15 修复
- 证据：`FmsVoucherServiceImpl.java:297-298`
  ```java
  voucher.setVoucherNo("JZ" + LocalDate.now().format(...yyyyMMdd)
          + System.currentTimeMillis() % 10000);
  ```
- **修正原判断**：初次评审称"同秒并发必重号"不准确。实测机制是——`currentTimeMillis() % 10000` 取值空间仅 1 万且**每 10 秒循环一次**，同日两笔凭证落在同一余数即撞号；且 `biz_fms_voucher` 已有 `uk_no(voucher_no, tenant_id)` 唯一键，撞号不会静默重复，而是插入报错（前端 500）。问题真实存在，但严重度低于初判。
- 真正的缺陷在幂等：`:125-139` `createAutoPosted` 仅靠 `selectBySource` 先查后插，`(source_type, source_id)` **无任何数据库约束**，并发下同一来源单据确可生成两张凭证。
- 修复：凭证号改用 `BizDocumentNo.nextShort("JZ")`；新增 `uk_source(tenant_id, source_type, source_id)` 唯一键；`createAutoPosted` 捕获 `DuplicateKeyException` 转为返回已存在凭证。

**P0-3 生产密钥明文落盘，与自己的部署规范相矛盾**
- 证据：`deploy-backup/application-pro.yaml:30/35/41`（MySQL/Redis 密码）、`:85/92`（微信 secret）；根目录 `application-private.properties` 真实微信/钉钉/快递鸟密钥
- 矛盾点：`DEPLOY-ALIYUN.md:44` 明确要求"密码用环境变量注入（不要明文进 git）"，但备份文件实际是明文
- 说明：虽已 gitignore 未入库（这点做对了），但明文落盘 + 备份目录回拷机制会持续扩散
- 修复：pro 配置改 `${ENV_VAR}`；备份文件加密或改存密钥管理服务；**轮换已落盘的所有 secret**

**P0-4 构建临时产物与失效配置入库**
- 证据：`git ls-files` 命中 `tmp_biz.jar`（298KB），`.gitignore` 无对应规则
- 证据：`enterprise-server/Dockerfile:3,9` 使用 `eclipse-temurin:21-jre` 且 `COPY ./target/yudao-server.jar`，而实际 `finalName=enterprise-server`（`pom.xml:198`）、JDK 为 17 → **该 Dockerfile 必然构建失败**
- 修复：`git rm --cached tmp_biz.jar` 并补 `.gitignore`；修正或删除 Dockerfile

### P1 · 重要

**P1-1 补卡 BPM 回调无事务且吞异常，与本地审批路径语义不一致** ✅ 已于 2026-09-15 修复
- 证据：`AttendanceCorrectionServiceImpl.java:72` `updateCorrectionStatusFromBpm` **无 `@Transactional`**；`:79` 状态已 CAS 落库后，`:87-93` 回写失败仅 `log.error` 吞掉
- 对照：`:127` 的 `auditCorrection` 有 `@Transactional(rollbackFor = Exception.class)`，回写失败会整体回滚
- 影响：BPM 路径下出现"补卡已通过但考勤未写"的静默数据不一致
- 附带问题：`:76` `status - 1` 做状态映射但**无范围校验**，非法值会写入越界状态
- 修复：加 `@Transactional` 并让异常抛出；映射改终态白名单（`APPROVE(2)->1`、`REJECT(3)->2`），其余跳过
- **执行时补充的重要发现**：BPM 状态事件在审批事务内**同步**发布（`BpmProcessInstanceServiceImpl` 用 `TransactionSynchronizationManager.registerSynchronization`），
  故该方法的事务会加入审批事务——考勤回写持续失败会导致审批一并回滚。这是刻意取舍：**宁可审批失败重试，也不留静默不一致**。
  本报告原先建议的"失败落异常表重试"是更优解，但需新建表与补偿任务，归入阶段二。
- **执行时新发现的缺陷（原评审遗漏）**：`CorrectionStatusListener:37` 第三个参数应为流程实例编号，原代码传 `event.getBusinessKey()`，
  会把 `process_instance_id` 写成补卡单 id。已改为 `event.getId()`。

**P1-2 信用额度校验可被并发绕过**
- 证据：`SalesServiceImpl.java:90-113` `transitionSales` 无事务无锁；`CreditService.java:58-68` 仅读快照
- 影响：同客户两张销售单并发 confirm 可双双通过 → **超额放货**
- 修复：确认时锁客户行（`selectForUpdate`），或建应收台账做 CAS 累加

**P1-3 无 CI/CD、无静态扫描门禁**
- 证据：无 `.github/workflows`、无 `.gitlab-ci.yml`、无 `Jenkinsfile`；无 SonarQube/SpotBugs/Checkstyle/PMD/JaCoCo；无 `.editorconfig`
- 部署方式：手工 `scp` + `systemctl restart`（`DEPLOY-ALIYUN.md:50`），`:87-90` 自述"Actions 后续可加"
- 影响：质量完全依赖人工审查；本项目正是因为"外部审查"才发现加固项——说明门禁确实缺位

**P1-4 测试覆盖严重偏科**
- 证据：`enterprise-module-biz/src/test` 下**仅 1 个文件** `BizConcurrencyTest.java`；且 `:46` 需 `BIZ_TEST_JDBC_URL` 环境变量才运行（否则静默跳过）
- 覆盖：payment/sales/leave/return/stock/quota 并发
- **零测试**：WMS、FMS、CRM CRUD、考勤、报销、Portal、Dashboard
- 前端：`HARDENING:95` 自述 `ts:check` 未通过，50 条存量类型错误
- 影响：新加的 FMS/WMS 两个大模块（恰恰是重灾区，见 P0-2）没有任何自动化保护

**P1-5 全表捞内存 + N+1 查询**
- 证据：`FmsVoucherServiceImpl.java:185-192` 取全部已记账凭证再取全部分录、`:231-246` 按日期过滤却在内存做；`BusinessServiceImpl.java:102` `selectListAll()`；`DashboardController.java:77-97` 多次 `selectList().size()` 统计；`FmsVoucherServiceImpl.java:306-321` `saveEntries` 循环内 `accountMapper.selectById`（N+1）
- 影响：数据量上去后 OOM 与响应劣化。当前是空库/小数据所以没暴露

**P1-6 事务内做远程调用**
- 证据：`WmsStockServiceImpl.java:191` `insertMove` 在 `putaway/remove/move`（`:88/112/125` 均 `@Transactional`）内调 `adminUserApi.getUser()`，RPC 期间**持有产品行锁**
- 影响：RPC 抖动直接放大锁等待，连锁阻塞入库/出库
- 修复：前置查询，或把用户信息查询移出事务

**P1-7 迁移工具校验和设计有缺陷**
- 证据：`tools/migrate.py:241` 校验和取**脚本自身哈希**，而非 SQL 文件内容哈希 → 改动工具代码即触发"checksum changed"误报；`:243,253,275` V002 版本号硬编码
- 修复：校验和绑定 SQL 文件内容；版本号从目录扫描

**P1-8 部分接口无权限注解**
- 证据：`AnnouncementController.java:57`(/get)、`:72`(/list-latest)、`FmsVoucherController.java:72`(/get)、`FmsAccountController.java:68`(/simple-list)、`WmsLocationController.java:68`(/simple-list)
- 说明：多数是刻意的下拉/详情接口（登录即可），但**缺少注释说明意图**，未来容易被误当作漏洞或被无意识扩大
- 修复：补 `@PreAuthorize` 或统一加注释标注"刻意开放"

**P1-9 AI 模块是死代码**
- 证据：`pom.xml:22` `<module>enterprise-module-ai</module>` 被注释；`enterprise-server/pom.xml:121/170` 依赖同样注释。但 `src/` 下 3 个 Java 文件（RagService/RagController/VectorStoreConfig）真实存在，且 `docs/RAG-LEARNING-GUIDE.md` 已配套写好
- 影响：代码在盘但从不编译，会随时间腐坏；文档承诺的能力实际不可用
- 修复：明确决策——要么纳入构建（补依赖 + 配置 + 菜单），要么移出到独立实验目录

**P1-10 Actuator 端点全暴露**
- 证据：`application-dev.yaml:130` `include: '*'`
- 影响：生产环境信息泄露面（健康、指标、环境变量）
- 修复：pro profile 收敛为 `health,info,metrics` 白名单

**P1-11 lombok 版本不一致**
- 证据：根 `pom.xml:55` = `1.18.42`（注解处理器）vs `enterprise-dependencies/pom.xml:61` = `1.18.46`（依赖）
- 影响：处理器与运行时不一致，可能产生难以定位的编译/运行差异

**P1-12 内部工具存在未闭环安全问题**
- 证据：`.mimosa/reports` 累计 2 条 medium——`tools/ssh_exec.py:37` 证书校验缺失、`:70` 命令注入（状态仍为 `candidate`）
- 修复：修复后复验，并闭环台账

### P2 · 建议

| 编号 | 问题 | 证据 |
|---|---|---|
| P2-1 | 业务逻辑堆在 Controller | `PortalController.java:120-169` 打卡状态机+配额校验；`DashboardController.java:35-98` 注入 11 个 Mapper 直接做统计 |
| P2-2 | 魔法值散落 | `SalesServiceImpl.java:74/95/120/148` `"0"/"1"/"2"`；`ReturnServiceImpl.java:43-50`；`ExpenseServiceImpl.java:74` `status-1` 无范围校验 |
| P2-3 | CRUD 样板重复 | `CustomerServiceImpl`/`ProductServiceImpl`/`SupplierServiceImpl` 各 67 行结构完全一致；全模块 91 个 VO |
| P2-4 | 死代码 | `DashboardController.java:72-73` `pending` 定义后未使用 |
| P2-5 | 预发布依赖 | `hutool 6.0.0-M22`、`weixin-java 4.8.6-20260825.155844`（时间戳快照版本） |
| P2-6 | 品牌残留 | `pom.xml:43` url 仍指向 `github.com/YunaiV/ruoyi-vue-pro`；`server.log` 含 `${yudao.info.version}` 与"芋道源码" |
| P2-7 | 仓库卫生 | `server.log`(496KB)、`migration-issues.json`(108KB) 未被 git 跟踪（这点对），但常驻工作区易误提交；建议统一移入 `.enterprise-pro-work/` |
| P2-8 | 文档缺口 | 缺架构图、领域模型图、接口契约文档；`docs/` 仅 2 份文件 |

### 未发现问题的项（明确澄清）

- **金额精度**：全量 `BigDecimal`，无 double/float ✓
- **NPE 裸链**：无 `getX().getY()` 未判空链式调用 ✓
- **SQL 注入**：无 Mapper XML，`@Select` 全 `#{}`，`apply/last` 均为固定常量 ✓
- **租户隔离**：33 个 DO 全覆盖 `TenantBaseDO` ✓
- **分页**：29 个 `*PageReqVO` 全 `extends PageParam`，无遗漏 ✓
- **行锁/CAS**：`StockMapper.java:30-32`、`WmsLocationStockMapper.java:42`、`SalesMapper.java:52-57` 设计正确 ✓
- **异常处理**：无 `printStackTrace`、无空 catch ✓
- **第三方 SNAPSHOT**：无（仅有预发布版，非 SNAPSHOT）✓
- **敏感文件误入库**：仅 example 模板入库，实际密钥文件已 gitignore ✓

---

## 五、改善方案

### 阶段一 · 止血（建议本周内完成，成本极低、收益极高）

| # | 动作 | 对应问题 | 验收标准 |
|---|---|---|---|
| 1 | 前端 `pro-ui` 推送到自有仓库，`origin` 改指自有仓库 | P0-1 | `git branch -vv` 显示上游跟踪 |
| 2 | `git rm --cached tmp_biz.jar` + 补 `.gitignore` | P0-4 | `git ls-files \| grep jar` 为空 |
| 3 | 修正或删除 `enterprise-server/Dockerfile` | P0-4 | Docker 构建通过，或文件移除 |
| 4 | 凭证号改用 `BizDocumentNo.next("JZ")` + `(source_type,source_id)` 唯一索引 | P0-2 | 并发压测无重号、无重复凭证 |
| 5 | pro 配置改 `${ENV}` 注入，轮换已落盘 secret | P0-3 | 备份目录无明文密码 |
| 6 | 补卡 BPM 回调加 `@Transactional`，失败落重试表 | P1-1 | 故障注入测试下状态与考勤一致 |

### 阶段二 · 工程化（2–4 周）

1. **CI/CD 落地**：GitHub Actions 三段流水线 —— `build`(mvn package) → `test`(启动 MySQL service 跑 `test_biz.py` + `test_migrations.py`) → `deploy`(SSH 推 jar 重启 systemd)。把 `DEPLOY-ALIYUN.md:87-90` 的"后续可加"变成现实。
2. **质量门禁**：Spotless（格式化）+ SpotBugs（缺陷）+ JaCoCo（覆盖率阈值）。先只告警不阻断，跑两周让存量问题浮出来再决定基线。
3. **补齐测试**（按风险排序）：FMS 凭证生成幂等与借贷平衡 → WMS 库位守恒 → 信用额度并发 → 考勤/报销。目标：`enterprise-module-biz` 测试文件从 1 个增到 8+。
4. **修前端 `ts:check`**：50 条存量类型错误清零，纳入 CI。
5. **AI 模块决策**：纳入构建（补 `spring-ai-ollama` 依赖 + 配置 + 菜单 + 向量库文件目录）或移出主仓库。
6. **收敛 Actuator**、统一 lombok 版本、闭环 `.mimosa` 的 2 条 medium。

### 阶段三 · 架构优化（1–2 个月）

1. **查询下推**：`FmsVoucherServiceImpl` 的余额/报表改为 SQL 聚合（`GROUP BY account_id` + 日期条件下推），`DashboardController` 统计改为单条聚合 SQL，`saveEntries` 改 `selectBatchIds` 批量取科目。目标：报表接口在大数据量下仍 < 200ms。
2. **Controller 瘦身**：`PortalController` 打卡状态机、`DashboardController` 统计逻辑下沉到 Service，Controller 只做参数校验与编排。
3. **抽公共基类**：`CustomerServiceImpl`/`ProductServiceImpl`/`SupplierServiceImpl` 三份 67 行同构代码抽 `AbstractCrudService`，VO 用 MapStruct 收敛。
4. **魔法值治理**：状态码改枚举（`SalesStatusEnum` 等），配套字典。这同时能根治 P1-1 的 `status-1` 越界问题。
5. **可观测性**：接入 SkyWalking 或 Micrometer + Prometheus，把"每日 09:00 定时任务""RPC 耗时""慢 SQL"变成可观测指标。
6. **文档补全**：架构图（C4 或分层图）+ 领域模型 ER 图 + 核心接口契约（OpenAPI 导出）。

---

## 六、总体评价

**一句话**：这是一个**工程质量明显高于同类脚手架改皮项目**的系统——业务建模有深度、并发处理有意识、文档习惯罕见地诚实；主要短板集中在**工程化基础设施缺失**（CI/门禁/测试覆盖）和**迭代速度带来的技术债沉积**（FMS/WMS 两个新模块引入了与项目既有标准不一致的实现）。

**风险画像**：
- 业务正确性风险：**中低**（核心资金/库存路径有 CAS + 行锁 + 幂等保护）
- 数据丢失风险：**高**（前端无远程备份，P0-1）
- 安全风险：**中**（密钥明文落盘，但未入库；租户隔离与权限基线良好）
- 可维护性风险：**中**（无 CI/门禁/测试偏科，新人上手成本高）
- 性能风险：**当前低、增长后高**（全表捞 + N+1 在小数据量下不可见）

**优先建议**：先把阶段一 6 项做完（预计半天到一天工作量），能消除全部 P0；再投入阶段二建立 CI + 测试，让后续迭代有安全网。

---

*报告生成：2026-09-15 · 基于实际代码与仓库状态核查，非推测*
