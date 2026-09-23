# enterprise-pro（企业平台后端 · yudao master-jdk17 架构）

> 共享规则（大动作先征求同意等）见 `..\AGENTS.md`。本文件随 git 提交，**禁止写入密码/密钥**。
> 历史档案：`docs/AI-MEMORY-ARCHIVE.md`（只读）；权威文档：docs/HARDENING-2026-09-11.md、DEPLOY-ALIYUN.md、docs/CODE-REVIEW-2026-09-15.md。

## 技术栈与运行

- Spring Boot 3.5 / JDK17 / 端口 48080；包名 `com.enterprise.*`；模块 enterprise-module-biz / bpm / im + framework 等；主类 EnterpriseServerApplication
- 登录 admin + 本地开发密码（见个人 `~\.zcode\AGENTS.md`，本文件随仓库提交不写密码）+ header `tenant-id: 1`；MySQL 库名 `enterprise-pro`；本地 Redis 用 Windows 服务 RedisEnterprise
- 编译坑：根 pom 与 enterprise-dependencies 已显式 `<release>17</release>`（覆盖全局 settings.xml 的 jdk-1.8 profile，勿改用户全局配置）；**lombok 版本必须与 BOM 对齐（annotationProcessorPaths 同版，现为 1.18.46）**
- 凭据一律 `${ENV:}` 外置（application-private.properties 本地 / 服务器 `/data/app/enterprise.env` + `config/application-pro.yaml`），缺变量启动即失败（刻意设计）
- mvn package 前必须先停本地后端（jar 文件锁）；EasyExcel 包名是 `cn.idev.excel`（FastExcel，不是 com.alibaba.excel）

## 生产（阿里云 ECS 8.155.128.225）

- 栈：systemd `enterprise`（-Xmx2g）/ MySQL8 / Redis / nginx；前端 /data/enterprise/front；移动工作台 `/m/`（enterprise-pro-app uni-app，nginx alias）
- 部署流程：本地改 → git commit → 构建 → 云上备份（/data/backup/{jar,front,db}）→ scp → 导 SQL → `systemctl restart enterprise` → 云上冒烟。**严禁直接在服务器改代码**（本地仓库是唯一事实源）
- **前端发版必须先 `npm run build:prod` 再 tar dist-prod，禁复用上次构建产物**
- 后端启动约 2.5 分钟（2C8G），慢不是故障；nginx 需含 /infra/ws WebSocket 升级配置（deploy-backup/enterprise.conf）与 index.html no-cache（防旧缓存 404/白屏）
- yudao 对未知路径返回 HTTP 200 + code:404 JSON 壳——判错要看响应体/`_links`，别看状态码
- 生产 DB 应用凭据在服务器 `/root/enterprise-credentials.txt`；迁移工具 tools/migrate.py 必须放 tools/ 子目录运行（按 parents[1] 定位 SQL）
- **旧版 318 项 smoke_test 不可在生产业务库跑**；生产验证 = 16 项 JUnit（tools/test_biz.py 自建 QA 库）+ 手动 API 抽查

## 关键路由/接口备忘

- 进销存真实路由 `/biz/inventory/*`；CRM 三级链 `/biz/crm/*`；WMS `/biz/inventory/wms-*`（菜单路由=父链拼接，直接拼短路径会 404）
- 销售/采购流转：`POST /biz/sales/transition?id&action=confirm|void` + `POST /biz/sales/complete?id`；一步走 `create-and-complete`；冲销 `POST /biz/payment/reverse` body {id,reason}
- 报销审批 `POST /biz/approval/expense-audit`（PUT 会 405）；报销创建 `POST /portal/expense-submit`
- BPM：bpmnXml 仅 type=10 设计器才保存；BPM 状态 2=通过/3=驳回需映射到业务状态；警惕 `"1".equals(Integer)` 恒 false 类坑
- 手工凭证筛选= `source_type IS NULL`（不是 'manual'）

## 业务规则速记

- 库存联动在单据"完成"流转触发（建单不动库存）；biz_stock_move 流水只增不改、禁删（冲销替代）；信用额度**确认即占用**；移动加权成本=(原库存×成本+本次金额)/(原库存+本次)，**cost 为 NULL 时勿用 Map.entry()（NPE）**
- 生成器 tools/gen_biz_yudao.py（重跑清空 biz src，手改需同步模板）、tools/gen_front.py；system_menu 主键列名是 `id`
- 字典/菜单是全局表**无 tenant_id 列**，种子 SQL 勿加该列；INSERT...SELECT 判重 NOT EXISTS 时 LIMIT 放最后
- BeanUtils 同名拷贝之外的字段（purchaseCode→sourceCode 类）必须手动映射，漏了=查询字段全 null

## 当前状态（2026-09-15）

- 生产运行 ID 化版 + P0/P1 评审修复（biz_schema_history V001+V002 已 apply）；前端 pro-ui 已推远程（仓库重建单 commit 7fa85ab）
- 后端 14+ commit 未推 GitHub（Push Protection/网络积压）；AI/RAG 已重新纳入 Maven 构建链，功能默认关闭，启用条件见下方记录
- AI/RAG：`enterprise-module-ai` 已恢复到 Maven 构建链，RAG 默认关闭；启用前先人工执行 `sql/mysql/ai_rag.sql`，配置 Ollama/Qdrant 环境变量，并完成构建及本地联调。禁止在无明确授权时执行生产 DDL 或启动生产 RAG。
- 测试样例：业务流程首版语料在 `docs/rag-test-corpus/企业业务流程测试资料.md`；RAG 服务单测可用 `mvn -pl enterprise-module-ai -am -Dtest=RagServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`。真实联测使用 opt-in `RagServiceIntegrationTest`，默认只允许写入 `enterprise_pro_qa_rag_*` MySQL 库和 `enterprise_knowledge_qa_*` Qdrant collection；只有显式设置 `RAG_TEST_ALLOW_LOCAL_DEV_DB=true` 时才允许连接精确的 loopback `enterprise-pro`，并且 Qdrant collection 必须是 `enterprise_knowledge`。2026-09-23 本机联测通过（MySQL + Ollama + Qdrant，27 个向量、引用与跨库隔离均通过）。AI 菜单初始化必须用 `status=0` 启用（`CommonStatusEnum`：0=ENABLE、1=DISABLE）；知识库相关菜单 id 791、850-854 已在 V001 修正。本机演示运行地址：后端 `http://127.0.0.1:48080`、前端 `http://127.0.0.1:5173`，登录后打开 `/ai/knowledge`。全量依赖测试当前会被既有脱敏断言（DesensitizeTest）失败拦住。
- 遗留待办：CI 启用（工作流暂在 docs/ci/，待 PAT workflow scope）、captcha.enable=true（正式上线前）、kd100 凭证真实性确认+轮换
- 下批候选：RAG PDF/Office 解析与异步任务、销售报价单、月度结账、通知邮件/webhook、批次效期

## 远程仓库分支约定（testrepository · push 前先确认分支名）

| 分支 | 承载 | 说明 |
| --- | --- | --- |
| `main` | enterprise-pro（后端） | 本仓库，跟踪 origin/main |
| `pro-ui` | enterprise-pro-ui（Web 前端） | 重建仓库单 commit 7fa85ab |
| `app` | enterprise-pro-app（移动端） | 从 app 仓库推入 |
| `master` | 历史遗留（旧 enterprise-ms） | **勿动勿推**，误推会覆盖旧 ms 历史 |

> 长期方向是三项目独立仓库（见 docs/REVIEW-2026-09-20.md S5）；现阶段以分支命名空间防误覆盖为准绳。
