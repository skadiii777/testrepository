# 更新日志（CHANGELOG）

## 2026-09-23（三）· 生产逐页走查（103 页）

- 从 DB 菜单表重建路由清单（108 条含 5 条孤儿），浏览器逐页走查：路由可达、404 兜底、错误 toast、渲染体积四项检查
- **发现并修复 1 项**：品牌清理时父菜单 83「代码生成案例」停用，但其 5 个子菜单（536/537/543/549/555，infra/demo 上下游示例页）仍为启用——菜单树过滤后成孤儿（侧栏不可见、直接访问 404）。已按父级状态对齐停用（`sql/mysql/demo_orphan_menu_disable.sql`，幂等）
- 其余 103 页全部正常加载：系统/基础设施/BPM/AI 知识库（/ai/knowledge 新页可达）/IM/CRM/进销存/HR/协作/FMS/WMS/看板/门户
- **AI 知识库页为空壳属预期**：`ENTERPRISE_AI_RAG_ENABLED=false` 时 RagController 条件装配未注册，列表接口 404 静默，页面骨架正常渲染；开开关后即有内容
- 移动端 /m/ 抽测正常（登录页渲染、无 404）
- 方法备忘：走查路由要从 DB 父链完整拼接（漏中间层会误报 404）；页面上多表单并存（登录/手机/注册）时自动化要按可见性定位

## 2026-09-23（二）· RAG 上生产：基础设施就位，RAG 开关待模型 API

- **Qdrant 生产安装**：最新版二进制需 GLIBC 2.38（服务器 Ubuntu 22.04 只有 2.35），降级 **v1.10.1** 兼容版装 /usr/local/bin + systemd `qdrant.service`（数据 /data/qdrant，仅监听 127.0.0.1:6333/6334，无 api-key）；GitHub 大文件下载慢，用 `-C -` 断点续传多轮拉完
- **SQL 上生产**：`ai_rag.sql`（ai_knowledge_base/ai_knowledge_document 两表）+ `ai_menu_enable.sql`（791/850-854 status 0 启用；首版 `--（` 注释缺空格报 1064 已修）——生产侧栏出现「AI 大模型/AI 知识库」
- **新 jar 上线**（215MB，含 AI 模块与 spring-ai/qdrant 依赖）；`ENTERPRISE_AI_RAG_ENABLED` 保持默认 **false**：RagController @ConditionalOnProperty 未注册 → `/admin-api/biz/ai/rag/*` 404 为设计行为。**不能提前开开关**：`initialize-schema=true` 启动时会调嵌入模型探维度，模型 API 不在即启动失败
- 回归：登录/采购分页正常；内存充足（available ~5.1G）
- **待办（等用户模型 API）**：①确认 API 协议——Ollama 兼容则 `spring.ai.ollama.base-url` 指过去即可；OpenAI 兼容则需换 spring-ai openai starter；②`enterprise.env` 加 `ENTERPRISE_AI_RAG_ENABLED=true`；③全链路冒烟（建库→传文档→问答）

## 2026-09-23 · Ollama + Qdrant RAG 首期闭环

- 启用 AI 模块构建依赖，并以显式环境开关控制 RAG；Qdrant 保存向量，MySQL 保存租户知识库和文档元数据。
- 新增知识库创建/查询、UTF-8 TXT/Markdown 上传、文档查询/删除和带引用来源的问答；服务端按当前租户校验知识库并给向量检索添加租户与知识库过滤。
- 首期仅接收不超过 2 MiB 的 TXT/MD，文本上限 20 万字；SQL 结构见 `sql/mysql/ai_rag.sql`。部署前需人工执行迁移并配置 Ollama、Qdrant 与 `ENTERPRISE_AI_RAG_ENABLED=true`。
- 补充首轮业务流程语料 `docs/rag-test-corpus/企业业务流程测试资料.md`，覆盖销售、采购、请假、报销和库存流水，并列出预期问答及空库隔离场景。
- 修复知识库页面 404：`CommonStatusEnum` 中 0 表示启用，1 表示禁用；初始化 SQL 曾把 AI 菜单及权限（791、850-854）设为禁用。现已修正 `V001__seed.sql`，并启用本机开发库对应菜单。
- 验证：RAG 服务单测 4 项通过；真实 MySQL + Ollama + Qdrant 端到端测试通过，语料生成 27 个向量，文档状态为 READY，空知识库跨库隔离通过。自动化数据写入本机隔离库 `enterprise_pro_qa_rag_20260923_1138` / QA collection `enterprise_knowledge_qa_20260923_1151`；经本机页面联调授权后，也将样例写入本机开发库 `enterprise-pro` / collection `enterprise_knowledge`。本机页面现可访问，已从页面提交销售库存问题并看到回答与 `企业业务流程测试资料.md` 引用。后端健康检查 UP，前端地址 `http://127.0.0.1:5173`，知识库路由 `/ai/knowledge`。新增页/API ESLint 通过。完整依赖测试仍被既有 `DesensitizeTest` 掩码格式断言阻断，前端全量 TypeScript 检查仍有多个既有错误。

## 2026-09-20 · 复核报告（docs/REVIEW-2026-09-20.md）方案落地

- **S1 移动端远程备份（最高优先）**：app 仓库补 package.json 元信息 + README/AGENTS，推送 testrepository `app` 分支成功（`master -> app`，本地 master 跟踪 github/app）；三项目单点风险全部消除
- **S3 规矩文件入库**：BE/UI 双仓 `.github/ AGENTS.md CLAUDE.md` + `docs/REVIEW-2026-09-20.md` 入库（入库前扫敏脱敏 3 处：admin123/-p123456/TVDBZ 地图 key）；`docs/AI-MEMORY-ARCHIVE.md` 按"工具私有记忆只是缓存"入 .gitignore；UI 按约定暂不推送远程
- **S4 文档修复**：README 模块表补齐 FMS/WMS/IM/CRM 扩展 + 登录凭据场景化；DEPLOY-ALIYUN 修 2C8G/mysqldump 不落盘/build:prod+.env.prod/nginx 段改指 `enterprise.conf` 并显式警告过期的 `nginx-enterprise.conf`（修正 4）；MIGRATION-STATUS 加归档声明；UI README 去上游化重写（版本更正 Vue 3.5.34/Vite 8.1.4/EP 2.13.7/TS 6.0.3）
- **S5B 分支约定**：AGENTS.md 新增 testrepository 分支表（main/pro-ui/app，master 历史遗留勿动）；AGENTS.md 自身脱敏
- **代码修复**：P1-6 `insertMove` 操作人昵称改 ConcurrentHashMap 缓存（事务持锁期最多一次 RPC，operator_name 历史留名语义不变）；P1-7 `migrate.py` 校验和绑定迁移函数源码（inspect.getsource，无关改动不再误报）+ 版本号收敛常量 + 历史记录不匹配降级提示；S6 tencent-lbs-key 参数化（原值落私有 properties，真实性/轮换待用户判断）；P2-6 pom url 改自有仓库；N8 captcha 键名统一 `enterprise.captcha.enable`；N7 .env.prod 删两行残留
- **部署冒烟**：新 jar 上线（备份后重启 ~90s），采购 0.01→凭证 `JZ260920160414NZ`；WMS 建库位→putaway→remove→清理全链路通过，move 表 id=5/6 operator_name 正确写入（缓存路径两次操作均生效）
- **冒烟脚本坑**（记入 AGENTS）：putaway/remove 字段不同（locationId vs fromLocationId）；库位 create 必填 type；历史冒烟库位已软删（deleted=1），`WHERE deleted=0` 查不到属正确行为

## 2026-09-15 · 上节修复部署实况（8.155.128.225）

- 新 jar（lombok 1.18.46 构建）+ 外置 `application-pro.yaml`（Actuator 固化段）已上生产；备份 jar/yaml 后重启 ~90s 起
- 验证：`/actuator` 发现列表仅 health，`/actuator/env|beans` 未暴露——注意其 HTTP 状态码为 200，系 yudao 全局兜底（code:404 JSON 壳），**勿以状态码判断暴露面**；`/actuator/health` = UP
- 业务回归：采购 0.01 完成 → 凭证 `JZ2609151500253Z`（16 位新格式，借贷平）；登录/采购分页正常
- 另核实：前端 pro-ui 已推远程（7fa85ab 本地=远程，历史重建为单 commit，P0-1 消除；GitHub 直连需 `-c http.version=HTTP/1.1`）；CI 工作流暂置 docs/ci/（PAT 缺 workflow scope，待补后移入 .github/workflows）

## 2026-09-15 · 阶段二小项：lombok 版本对齐 + Actuator 暴露面固化

- **lombok 版本不一致**（P1-11）：根 `pom.xml` 的 `lombok.version` 为 `1.18.42`（用于
  maven-compiler-plugin 的 annotationProcessorPaths），而 `enterprise-dependencies/pom.xml`
  的 BOM 为 `1.18.46`（实际进入 classpath 的依赖版本）——**注解处理器与运行时依赖不同版本**。
  已统一为 `1.18.46`，并在两处加注释说明必须保持一致。
  验证：全项目 `mvn -DskipTests compile` → BUILD SUCCESS（3:13）。
- **Actuator 暴露面**（P1-10）：复核后**更正原评审判断**——`include: '*'` 只存在于
  `application-dev.yaml` 与 `application-local.yaml`，`application-pro.yaml` 无 `management` 段，
  生产走 Spring Boot 默认（仅 `health`），**并非生产信息泄露**。
  本次仍做加固：在 `application-pro.yaml` 显式写入
  `management.endpoints.web.exposure.include: health`，不再依赖默认值，
  避免日后有人把 dev 配置上移到底层 yaml 时静默扩大暴露面。

## 2026-09-15 · CI 就绪（阶段二起步，待启用）

- 新增 `docs/ci/github-actions-ci.yml`：`push`/`pull_request`（main）与手动触发。
  - **build**：JDK 17 + Maven 缓存 → `mvn -B -DskipTests compile` → `mvn -B -DskipTests package`
  - **secret-scan**：扫描受版本控制的配置文件是否残留明文密钥（正则覆盖
    password/secret/api-key/key/customer/request-key/response-key/client-secret）
- 两个 job 的命令与规则均已在本地实测通过：全项目 `compile`（3:55）与 `package`（3:45）各
  BUILD SUCCESS；扫描规则在**当前仓库 0 命中**（印证阶段一的密钥参数化完整），并用含明文密码的
  样例文件做反向验证确认规则有效。
- ⚠️ **尚未启用**：工作流须位于 `.github/workflows/` 才会生效，但本机 PAT 权限为
  `X-OAuth-Scopes: repo`，**缺少 `workflow` scope**，GitHub 拒绝推送该路径下的文件
  （`refusing to allow a Personal Access Token to create or update workflow ... without workflow scope`）。
  故暂置于 `docs/ci/` 先纳入版本控制与备份，启用步骤见 `docs/ci/README.md`
  （方式 A：给 PAT 补 `workflow` scope；方式 B：改用 SSH，推荐）。
- 明确未纳入 CI 的项（避免误判为"已覆盖"）：单元/集成测试需真实 MySQL，待接入 CI 数据库服务；
  SpotBugs/Spotless/JaCoCo 存量问题未清理，宜先告警模式跑基线；前端为独立仓库需单独配置
  （当前 `ts:check` 尚有 49 条存量错误，其中 17 条为引用已删除 mall 模块的死代码）。

## 2026-09-15 · 阶段一加固上生产（8.155.128.225）

- 复核 f3ef53a/545cf55 两 commit 后按 DEPLOY-ALIYUN.md 新流程部署：`/data/app/enterprise.env`（600）注入 ENTERPRISE_PRO_* 五个变量，systemd 增 EnvironmentFile，`/data/app/config/application-pro.yaml` 换为无明文密码版，`application-private.properties` 增补 kd-niao business-id 与 kd100 key/customer（取 git 历史原值）
- 执行 `sql/mysql/fms_voucher_source_unique.sql`：重复来源检查为空，uk_no/uk_source 落库确认
- 备份：/data/backup/jar/{enterprise-server,application-pro-*,application-private-*}-<TS>；替换 jar 重启 ~90s 起
- 冒烟：采购 325 confirm→complete，凭证 `JZ2609151321544X`（16 位新格式，借贷 0.01 平，source=purchase/325，status=1）；补卡直批 id=304 通过；登录/租户接口正常
- 遗留：kd100 凭证真实性确认与轮换；captcha.enable=false
- **补记（09-15 下午）：前端 pro-ui 已推送远程，遗留项清除。** 推送失败的根因与网络无关——
  前端仓库是浅克隆（`.git/shallow` 边界 `aab14fb` 为合并提交，其父提交 `0f73d302`/`d1490e28`
  不在本地），推送时 git 发瘦包并假定远端已有父提交，而远端 `testrepository` 装的是后端历史，
  故服务端报 `fatal: did not receive expected object d1490e28...` → `remote unpack failed: index-pack failed`。
  排查过程中一次 `git rebase` 超时被强杀、中断了 git 自动 gc，导致该仓库 `.git/refs`、`.git/logs`
  与松散对象被清（原历史不可恢复，工作区源码无损）。已按当前工作区重建仓库并推送成功：
  `github/pro-ui = 7fa85ab`（1195 个文件 / 19.68MB），生产构建验证 `✓ built in 24.68s`。
  教训：在可能超时的前提下不得运行会触发 gc 的写操作（rebase/commit/gc），须加 `-c gc.auto=0`
  或置于后台给足时间。

## 2026-09-15 · 阶段一加固（评审 P0/P1 修复）

> 依据 `docs/CODE-REVIEW-2026-09-15.md` 的阶段一清单执行，目标为「消除全部 P0」。
> 已通过 `mvn compile -pl enterprise-module-biz -am` 全 19 模块 BUILD SUCCESS；SQL 在本地库实测通过。

### 修复

- **FMS 凭证号重号**（`FmsVoucherServiceImpl`）：旧实现 `"JZ" + yyyyMMdd + System.currentTimeMillis() % 10000`，
  取值空间仅 1 万且每 10 秒循环一次，同日撞号后由 `uk_no` 唯一键拦下并向前端抛错。
  改用与销售/采购单号同一生成器 `BizDocumentNo.nextShort("JZ")`（16 位，`varchar(32)` 内），撞号概率可忽略。
- **FMS 自动凭证幂等缺数据库兜底**：`createAutoPosted` 原为「先查后插」，并发下同一来源单据可生成两张凭证。
  新增 `uk_source (tenant_id, source_type, source_id)` 唯一键，并把并发冲突转为返回已存在凭证（捕获 `DuplicateKeyException`），
  不再向前端抛 500。手工凭证 `source_type/source_id` 为 NULL，MySQL 唯一索引允许多个 NULL，不受影响；
  自动凭证创建即已记账、`deleteVoucher` 拒绝删除已记账凭证，故无软删除占位问题。
- **补卡 BPM 回调静默不一致**（`AttendanceCorrectionServiceImpl.updateCorrectionStatusFromBpm`）：
  原实现无 `@Transactional` 且把考勤回写异常 `log.error` 吞掉，与本地直批路径 `auditCorrection`（有事务、失败回滚）语义不一致，
  会出现「补卡已通过但考勤未写」。现补事务注解并让异常向上抛出，两条入口语义对齐。
  > ⚠️ **取舍说明**：BPM 状态事件由 `BpmProcessInstanceEventPublisher` 在审批事务内**同步**发布
  > （`BpmProcessInstanceServiceImpl` 中有 `TransactionSynchronizationManager.registerSynchronization` 佐证），
  > 故本方法的 `@Transactional` 会加入该审批事务。若考勤回写持续失败，审批动作将一并回滚并向审批人报错，
  > 即"宁可审批失败重试，也不留静默不一致"。`correctTime` 有 `@NotBlank` 校验，正常路径不会出现
  > 确定性失败，实际失败源为数据库等基础设施问题——此时回滚重试正是期望行为。
  > 若后续希望审批不被阻塞，应改为"回写失败落重试表 + 定时补偿"，而非退回静默吞异常。
- **补卡 BPM 状态映射越界**：原 `status - 1` 无校验，`已取消(4)` 会写出状态 `3`、`未开始(-1)` 会写出 `-2`（均越界）。
  改为仅接受终态 `APPROVE(2) -> 1`、`REJECT(3) -> 2`，其余状态跳过回写并记日志。
- **补卡监听器传参错误**（`CorrectionStatusListener`）：第三个参数应为流程实例编号，
  原实现误传 `event.getBusinessKey()`，会把 `process_instance_id` 写成补卡单 id。改为 `event.getId()`。
- **构建产物入库**：`tmp_biz.jar` 已 `git rm --cached`；`.gitignore` 增加 `*.jar`（wrapper 除外）与 `.enterprise-pro-work/`。
- **Dockerfile 失效**：原用 `eclipse-temurin:21-jre` 且 `COPY ./target/yudao-server.jar`，
  与项目 JDK 17 基线和 `finalName=enterprise-server` 均不符，构建必失败。已修正为 17-jre + `enterprise-server.jar`。
- **配置明文密钥**：`application.yaml` 中 `kd100.key/customer`、`kd-niao.business-id`、
  `api-encrypt.request-key/response-key` 改为环境变量引用；`deploy-backup/application-pro.yaml` 的
  数据库/Redis 密码与微信 secret 改为 `ENTERPRISE_PRO_*` 环境变量注入。

### 部署影响（必须同步执行）

1. **服务器环境变量**：`application-pro.yaml` 不再含明文密码，部署前必须在 systemd 注入
   `ENTERPRISE_PRO_SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_PASSWORD`、
   `..._SLAVE_PASSWORD`、`ENTERPRISE_PRO_SPRING_DATA_REDIS_PASSWORD`（缺失将启动失败）。
   详见 `deploy-backup/application-pro.yaml` 文件头与 `application-private.example.properties` 尾部注释。
2. **数据库结构**：执行 `sql/mysql/fms_voucher_source_unique.sql`（幂等）。
   脚本第一步会输出重复来源凭证检查结果，**正常应为空**；若有结果须先人工核对删除多余凭证再建索引。
3. **凭证号格式变更**：新凭证号为 `JZ + yyMMddHHmmss + 2 位随机字母数字`（如 `JZ260915143052AB`），
   与历史 `JZ + yyyyMMdd + 4 位` 格式并存，不影响历史数据。
4. **待办（未在本次执行）**：`kd100.key/customer` 自首个提交起即存在于 git 历史并已推送 GitHub，
   需确认是否为真实凭证，若是则轮换；`deploy-backup/application-pro.yaml` 的 `captcha.enable` 仍为 `false`，
   上线前应改 `true`。

## 2026-09-13（续3）· WMS 仓储管理初步搭建（W1：库位 + 库位库存 + 流水）

- **定位**：库位库存是仓库库存的**分配视图**——上架/下架/移库只动库位库存表，不动主库存（biz_stock），零风险接入现有进销存；未分配量 = 仓库库存 − 库位分配合计。
- **库位管理**（biz_wms_location）：仓库+编码租户内唯一、类型字典（存储/拣货/收货/退货区）、有库存禁删、/simple-list 供动作下拉。
- **库位库存**（biz_wms_location_stock）：上架（未分配→库位，超分配拦截：产品行锁+未分配校验）、下架（库位→未分配，CAS 数量调整不足拦截）、移库（同仓库库位间，跨仓拒约）。
- **库位流水**（biz_wms_location_move）：只增不改，记录动作类型/源目标库位/操作人/备注。
- SQL：sql/mysql/wms.sql 幂等版（3 表+2 字典+菜单挂进销存管理目录，真实路由 /biz/inventory/wms-location、/biz/inventory/wms-stock）。
- 前端：库位管理页 + 库位库存页（库位库存/未分配/流水三 Tab，上架/下架/移库弹窗）。
- 验证：API 冒烟 20/20（唯一性/超分/守恒/移库/防呆删除/流水操作人）+ 浏览器上架端到端（流水入账、操作人"管理员"）。

## 2026-09-13（续2）· FMS Batch 3：进销存出入库自动凭证（赊购口径闭环）

- **采购入库**（完成时）：借 1405 库存商品 / 贷 2202 应付账款，金额=数量×采购单价。
- **付款口径修正**：付款凭证从"借库存商品"改为**借 2202 应付账款**（赊购标准口径——入库挂应付、付款冲应付，避免库存双重计入）；收款不变（借货币资金/贷收入）。
- **销售出库**（完成时）：借 6401 主营业务成本 / 贷 1405，金额=数量×产品标准成本（cost）；**未设置成本的产品跳过结转并告警**，请先维护产品成本。
- **退货执行**：采购退货出库 借 2202/贷 1405（按退货货值）；销售退货入库 借 1405/贷 6401 成本冲回（按产品成本）。
- 新增通用两行分录方法 `createSimplePosted`（科目编码解析+幂等+缺失降级告警内聚），四类业务单据一行接入。
- 账务闭环：入库挂应付→付款冲应付；出库结转成本→退货冲回成本；收入=实收（收付实现制简化口径），科目余额表可验证会计恒等式（资产变化=负债+权益变化）。
- 验证：本地全链路冒烟（采购入库/付款冲应付/出库结转/收款/两类退货凭证方向与金额/FIFO 红冲/恒等式平衡）+ 浏览器来源标签实测。

## 2026-09-13（续）· FMS Batch 2：收付款自动生成记账凭证

- **资金流水落库即入账**：收付款创建（SK/FK）、冲销（CX）、退货红冲（HK）三条路径自动生成**已记账**凭证（source_type=payment, source_id=流水 id），与收付款同事务，流水与凭证强一致。
- **分录规则**：收款=借货币资金/贷主营业务收入；付款=借库存商品/贷货币资金；负数流水（冲销/红冲）取反向分录。货币资金科目按收付方式映射：现金→1001，银行转账/微信/支付宝→1002。
- **红冲按方式抵减**：退货红冲按 合同×收付方式 拆分红字流水（FIFO），现金收款退现金、银行收款退银行，避免货币资金科目混记。
- **幂等与降级**：同一流水 id 只生成一张凭证（selectBySource 判重）；标准科目（1001/1002/1405/6001）被停用或删除时跳过生成并 log.warn，不阻塞资金主流程。
- 凭证列表新增**来源列**与来源筛选（手工录入=source_type IS NULL 特判）。
- 验证：本地冒烟（收款凭证方向/幂等不重复/冲销反向/现金→1001、银行→1002/退货 FIFO 红冲后三科目净变化为 0/手工筛选）+ 浏览器来源列与筛选实测。

## 2026-09-13 · FMS 财务管理 Batch 1（会计科目 + 记账凭证）

- **会计科目 CRUD**：编码租户内唯一（服务端校验+库内唯一键）、被分录引用/有子科目时禁止删除；`/simple-list` 供凭证分录下拉（登录即可）。
- **记账凭证**：创建时服务端强校验借贷合计相等；凭证号 JZ+日期+随机 创建即固定（编辑不重新生成）；草稿可改删，记账（post）后进入科目余额且禁改删，取消记账（unpost）回落草稿。
- **科目余额表**（GET /biz/fms/voucher/balance）：仅统计**已记账**凭证分录，按科目汇总借贷发生额与余额（借-贷），按编码排序；getTotalBalance 返回余额合计。
- SQL：sql/mysql/fms_voucher.sql 幂等版（表/3 组字典/16 标准科目/菜单均按唯一键判重）；注意上游自带停用的旧 FMS 演示菜单树（path=/fms，勿挂靠），菜单 parent 必须用新目录 id。
- 前端：科目管理页 + 凭证管理页（分录行编辑、借贷合计实时校验、凭证详情、科目余额弹窗）+ 3 组业务字典。
- 验证：API 冒烟 26/26（借贷不平拦截/草稿不入余额/记账防重/余额回落/科目防呆删除）+ 浏览器双页实测（新建提交/记账/余额）。

## 2026-09-11 · 第二、三轮加固（代码与升级工具）

- 收付款按单据串行处理，累计金额使用当前读；新增请求幂等与带原因的追加冲销，禁止删除资金流水，退货红冲保留合同与来源关联。
- 单号使用 UUID 与数据库唯一约束；历史重复编号保留原值。销售、采购手填单号同样受租户内唯一约束保护。
- 库存按产品/仓库 ID、配额及请假按员工 ID 关联；前端增加选择器和仓库维护入口，员工显式绑定登录账号。
- 补齐订单及请假编辑与审批的并发保护、BPM 回调防重、配额调低时的当前余额检查及已使用配额删除保护。
- 新增版本化迁移、空库初始化及管理员初始化工具；旧库歧义在改表前拦截。生成器输出隔离，避免覆盖业务实现。
- 共享配置与测试密码改为外部提供；SSH 工具要求已有可信主机记录，不自动接受主机密钥。
- 验证与发布限制详见 [升级说明](docs/HARDENING-2026-09-11.md)。本次未部署服务器，未修改业务库；历史 318 项结果属于上一轮验证。

### 2026-09-12 生产发布记录

- 已按发布顺序在正式服务器完成：全量备份（pre-id）→ 预检 176 项 → 删除重复 IM 测试账号 268 → 生成 175 条产品映射（取最早候选）→ `--apply` 迁移成功（V001+V002）→ 部署新 jar/前端 → 私有配置补回加密密钥与快递鸟 key。
- 生产验证：登录、销售/库存/收付款分页、引用选择器接口、已完成单据禁改（1050001044）全部通过；IM 重复测试账号 268 已删除，264 保留。
- 回滚方案：`/data/backup/db/enterprise-pro-pre-id-*.sql.gz` + `/data/backup/jar/` 上一版 jar。

## 2026-09-10（续）· 业务正确性加固（采纳外部审查第一轮）

### 加固（测试 302 → 318 项全过，本地+新服务器双认证）

- **数量/单价正数校验**：销售/采购单数量 `@Positive`、单价 `@DecimalMin(0.01)`，
  堵住"负数量完成出库变入库"的方向反转漏洞；**总金额一律服务端强算**
  （创建/修改均不信前端传入，修改时数量单价留空取库内原值重算）
- **状态流转 CAS 防并发**：确认/完成/作废改为带原状态条件的 UPDATE
  （`WHERE status=旧值`，检查影响行数，0 行抛"状态已变更"）——并发重复完成
  只有一个成功，杜绝双倍扣库存；完成先抢占状态再动库存，同事务回滚
- **已完成单据禁改删**：update/delete 前校验 status=2 则拒绝（纠错走退货/红冲）
- **请假审批防重**：审批 CAS（仅待审批 0 可流转，重复审批报"不能重复操作"）
  + 事务化（CAS 与扣减同事务，余额不足整体回滚）；扣减检查影响行数
  （并发余额不足不再出现"审批通过但未扣余额"）；销假同样 CAS 防重
- **前端状态流转入口补齐**（外部审查发现的最大闭环缺口）：销售/采购页面新增
  确认/完成出库·入库/作废按钮（按状态与权限显隐），状态字段改列表只读展示；
  完成弹窗说明库存联动
- 冒烟测试第 23 节 13 项断言（负数拒绝/重复完成/禁改删/总额强算/重复审批/
  余额只扣一次）

## 2026-09-10 · 模块联动增强（赢单转合同/低库存提醒/合同回款/退货红冲）

### 新增（测试 287 → 302 项全过，本地+新服务器双认证）

- **商机赢单一键转合同**（POST /biz/business/convert-to-contract）：仅赢单(5)可转，
  自动带入客户/金额/负责人，合同编号 HT+时间戳自动生成，状态=执行中，备注记
  转化来源；商机页赢单行出现「转合同」按钮+弹窗
- **低库存并入每日提醒**：到期提醒 Job 增加存量 ≤ 预警下限的产品扫描，
  站内信附建议补货量
- **收付款可选挂合同**：收款时可关联合同（与挂单据二选一——纯合同回款如收
  定金不再强制挂销售单，order_id/order_code 列放开可空）；合同列表新增
  **回款进度列**（已回款/合同金额 + 进度条）
- **退货自动红字收付款**：退货执行时自动生成红字流水冲减原单已收付
  （金额=min(退货货值, 原单累计已收付)，HK 前缀），资金口径与货权一致
- 部署：sql/mysql/linkage.sql（biz_payment 加 contract_id、order_id/order_code
  放开可空、biz_contract 加 remark、contract_id 索引）
- 服务器迁移：全量替换到新 ECS（2C8G，http://8.155.128.225/），
  本地库 mysqldump 全量导入 + Flowable 表大小写修复，287→302 回归全过

## 2026-09-09（续 6）· 修复 IM 实时推送（nginx WebSocket 升级头缺失）

- **根因**：云上 nginx 无 /infra/ws 的 WebSocket 配置，前端 WS 握手被 SPA
  兜底规则以 index.html 应答——握手从未成功，消息只能靠刷新拉取
- **修复**：nginx 增加 /infra/ws location（proxy_http_version 1.1 +
  Upgrade/Connection 升级头 + read/send_timeout 3600s），配置留档
  deploy-backup/enterprise.conf；后端握手认证与推送链路本就正常（本地 101 验证）
- **实测**：WS 客户端在线时，消息经 HTTP 发出后 **45ms** 即收到
  im-notification 推送帧（发送 36ms + 推送 9ms）；
  后端握手（refreshToken 鉴权）与拉取兜底均验证通过

## 2026-09-09（续 5）· IM 即时通讯上线（移植 yudao-module-im，300 文件）

### 新增（测试 276 → 287 项全过，本地+云上双认证）

- **enterprise-module-im**：好友（申请/同意/列表）、单聊（发送/增量拉取/历史/
  已读回执/撤回）、群聊（建群/邀请/申请/群消息/回执/置顶）、频道消息、表情包、
  会话已读、敏感词过滤、消息统计；RTC 音视频信令预留（LiveKit webhook，
  未启外部依赖）
- **实时推送**：复用已启用的 /infra/ws WebSocket（token 认证、心跳、断线重连，
  前端 websocketStore 已内置）；消息事务提交后按会话定向推送
- **前端零开发**：聊天界面 views/im/home 与 api/im 八组封装本就随 yudao-ui
  引入，本次仅在工作台快捷入口加「在线聊天」；管理页菜单树 1418 启用
- **SQL**：sql/mysql/im_tables.sql——17 张 im_ 表（上游单测 H2 脚本转 MySQL，
  幂等）+ 187 条 im 字典（highgo 种子提取，剔除 deleted_time 列）+ 菜单启用；
  infra_api_access_log.operate_name 扩长 varchar(50)→255（IM 超长接口摘要
  曾致日志写入 500）
- **修复**：ImChannelMessageMapper 空串条件 eq(getReceiverUserIds,"") 会把
  String 喂给 LongListTypeHandler 导致 ClassCastException——改 SQL 字面量
- 测试：冒烟 21 节（好友/私信/已读/建群/群消息 13 断言）；浏览器实测
  管理员与 testuser02 好友互聊（UI 发送落库、对方拉取可见）；云上 287/287
- 权限：聊天核心接口登录即可用（无 @PreAuthorize），管理页 im:manager:* 仅管理员

## 2026-09-09（续 4）· 登录安全：单账号单设备 + 在线用户检测

### 新增（测试 265 → 276 项全过；5 账号并发专项 19 项全过）

- **单账号单设备登录（互踢）**：登录创建令牌时自动清除同账号全部旧会话
  （access + refresh token 一起失效，OAuth2TokenServiceImpl.createAccessToken），
  同一账号后登录者生效、先登录者被踢，杜绝多人共用一个账号
- **在线用户检测**（系统管理 → 在线用户，system:online-user:*）：有效期内令牌
  = 在线会话，列表含用户名/昵称/部门/登录/过期时间与脱敏令牌，支持**强制下线**
  （按用户踢出，单设备模式下即其唯一会话）
- **关闭网页即失效**：前端令牌由 localStorage 改存 sessionStorage（auth.ts），
  关闭标签页/浏览器后必须重新登录；旧 localStorage 令牌在 removeToken 时一并清除
- 测试：smoke_test 第 20 节（互踢/在线列表/强制下线/重登，12 项）+
  tests/online5_test.py 独立专项（5 账号并发：全在线、无重复会话、admin 二次
  登录互踢后在线数不变、强制下线后在线数-1、被踢可重登，19 项）
- 部署：sql/mysql/online_user.sql（菜单）；无表结构变更（复用 oauth2 token 表）

## 2026-09-09（续 3）· 公司公告 + 到期提醒中心

### 新增（测试 253 → 265 项全过，本地+云上双认证）

- **公司公告 biz_announcement**（协作审批 → 公告管理）：标题/类型（通知/公告/
  制度）/正文/置顶/上下架；**工作台首页「公司公告」卡片**（复用组件
  AnnouncementCard，管理员与员工首页都展示，登录即可看，置顶优先，点开看详情）
- **到期提醒中心**：定时任务 bizExpiryReminderJob（每日 09:00，infra_job 已注册）
  扫描①执行中合同到期（30/7/1/0 天阈值各提醒一次）②超期未成交商机
  （预计成交日已过、未赢单/输单，每周一提醒）→ 站内信通知管理员
  （模板 biz_expiry_reminder）；POST /biz/dashboard/expiry-reminder 手动触发/补发
- **看板新增指标**：contractExpiringCount（30 天内到期合同）、
  businessOverdueCount（超期商机），企业概览新增两张预警卡
- 部署：sql/mysql/announcement_expiry.sql（公告表+字典+菜单+站内信模板+Job）
- 冒烟测试第 19 节 12 项断言（含造 7 天后到期合同→触发→站内信落库链路）；
  浏览器实测 UI 发布公告并在首页卡片可见；修复退货测试的类型不匹配用例
  （sales/purchase 自增 id 空间重叠导致的历史巧合，改用不存在单据 id）

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

- 登录验证码默认关闭（自动化测试需要），生产开启 `enterprise.captcha.enable`（嵌套键，实测配置即此写法；`captcha-enable` 为错误写法，设置的是不存在的属性）
- `application-druid.yaml` 数据源为明文密码，生产建议环境变量注入
- 生成器 `tools/gen_biz_yudao.py` / `tools/gen_front.py` 重跑会覆盖生成目录，手工改动需同步模板
