# RAG 知识库模块介绍（enterprise-module-ai）

> 日期：2026-09-23 ｜ 状态：生产基础设施已就位，模型 API 接入后开放
> 关联文档：`docs/AI-MODULE-INTEGRATION.md`（接入约定）、`docs/RAG-LEARNING-GUIDE.md`（原理教学）、`CHANGELOG.md 2026-09-23`

## 一、模块定位

为企业平台提供**租户隔离的知识库问答（RAG）**能力：业务方上传文档，系统分块向量化入库；提问时按相似度检索最相关的文本块，交给大模型生成**带引用来源**的回答。让"公司的制度/产品/合同知识"可以被自然语言直接问出来，而不是靠人翻文档。

## 二、架构与数据流

```
业务模块 ──(ObjectProvider 弱依赖)──> AiKnowledgeAssistant 契约接口
                                        │ enterprise-common/ai/api
                                        ▼
            enterprise-module-ai（RagService 实现）
             │                │                  │
        MySQL 元数据       Qdrant 向量库        Ollama 模型
   ai_knowledge_base    (gRPC :6334,      deepseek-r1:1.5b 聊天
   ai_knowledge_document 租户双重过滤)     nomic-embed-text 嵌入
```

**写入链路**：上传 `.txt/.md`（≤2 MiB）→ 文本分块（chunkText）→ 逐块生成嵌入向量 → 连同 `tenantId/knowledgeBaseId` metadata 写入 Qdrant → MySQL 记录文档状态（PROCESSING → READY / FAILED）。

**问答链路**：问题 → 嵌入 → Qdrant 相似检索（`filterExpression` 强制 `tenantId == ? && knowledgeBaseId == ?`）→ Top 相关块拼上下文 → LLM 生成 → 返回 `Answer(answer, citations[])`，引用可溯源到文档。

**关键设计决策**：

| 决策 | 说明 |
|---|---|
| 契约与实现分离 | 契约接口在 `enterprise-common`，业务模块不依赖 AI 实现类；RAG 关闭时业务照常启动，调用时得到清晰提示 |
| 整链条件装配 | `enterprise.ai.rag.enabled` 一个开关控制 Controller/Service/VectorStore 全链（`@ConditionalOnProperty`） |
| 双重租户隔离 | 应用层 `TenantContextHolder` 校验知识库归属 + 向量层 payload 过滤，杜绝跨租户检索 |
| 元数据与向量分离 | MySQL 只管租户资产（库/文档/状态/审计），Qdrant 只存向量与 payload；文档删除两侧联动 |
| 零信任外部依赖 | Qdrant 仅监听 127.0.0.1；模型地址/凭据全部环境变量注入，缺省快速失败 |

## 三、当前边界（首期）

- 文档格式：仅 UTF-8 `.txt` / `.md`，单文件 ≤ 2 MiB、正文 ≤ 20 万字
- 交互：同步问答（非流式）；单轮；每问必传知识库 ID（不跨库隐式检索）
- 模型：本地 Ollama（生产接入方式待定，见"升级展望"第 0 步）
- 不做：多轮会话记忆、文档权限分级、自动重排

## 四、业务模块接入预测（按价值/数据成熟度排序）

| 优先级 | 模块 | 接入场景 | 所需数据（现成度） |
|---|---|---|---|
| ★★★ | **人事考勤 + 协作审批**（portal 员工端） | 员工自助问答："病假扣薪怎么算""报销标准是什么""补卡流程"——制度/手册入库后，移动端工作台加一个"问制度"入口 | 员工手册、考勤/报销制度文档（需整理上传，1-2 天） |
| ★★★ | **CRM 客户合同产品** | 产品 FAQ、报价政策、售后条款入库 → 销售在客户页直接问"这款产品质保多久"；合同要点问答 | 产品资料/报价单/历史合同文本（已有大量结构化数据，导出整理即可） |
| ★★ | **审批中心** | 审批辅助：待审批单据自动摘要 + 按制度给风险提示（"该报销超出差旅标准"）——不只需要 RAG，还要结构化规则检索 | 制度文档 + 审批历史（数据在库） |
| ★★ | **公告/站内信** | 公告发布自动入库，过期公告自动失效；"最近出差补贴政策是什么"直接问 | 公告表已有（接口打通半天） |
| ★ | **IM 即时通讯** | 群机器人：@AI 问知识库，回答带引用发回会话 | 复用契约接口 + WS 消息通道（1-2 天） |
| ★ | **进销存** | 操作 SOP 问答（"怎么处理退货入库"）；后续结合 Agent 做单据填制助手 | 业务 SOP 文档（需整理） |
| 探索 | **数据看板** | 自然语言查数（Text2SQL，走只读视图 + 白名单字段）——不是 RAG 而是相邻能力，建议二期单独立项 | 只读视图 + 指标字典 |

> 通用接入方式：业务侧只写十几行（`ObjectProvider<AiKnowledgeAssistant>` 判空 + `answer(kbId, question)`），见 `docs/AI-MODULE-INTEGRATION.md` 示例。**前端复用** `src/api/ai/knowledge` 的 `askKnowledge`，引用结构已统一。

## 五、升级展望

**第 0 步（进行中）**：接入模型 API。Ollama 兼容协议只需改 `spring.ai.ollama.base-url`；若最终采用 OpenAI 兼容网关，需引入 `spring-ai-openai` starter 并做适配层——建议把"模型供应商"抽象成配置项，聊天/嵌入可分别指定。

**短期（1-2 批次）**：
1. **文档格式扩展**：PDF / DOCX 支持（Apache Tika 抽取），上传体积上限提升，批量上传 + 进度反馈
2. **流式输出**：问答改 SSE 打字机效果（Web 端 EventSource / 移动端分片渲染），显著改善体感
3. **检索调优**：topK / 相似度阈值可配；混合检索（关键词 BM25 + 向量，RRF 融合）；引用精确到段落锚点
4. **问答质量回归**：把 `docs/rag-test-corpus` 的预期问答固化为自动化评估集（命中率/引用正确率），每次换模型跑一遍

**中期**：
5. **多轮对话与意图澄清**（会话记忆、追问改写）
6. **知识库权限分级**：部门/角色可见性，普通员工问答自动限定在授权库范围
7. **租户配额**：每租户文档量/向量数/问答次数限额与用量统计
8. **生命周期管理**：文档版本化（重新上传即灰度替换向量）、过期自动下线、失效重建索引

**长期**：
9. **Agent 化**：从"问答"走向"办事"——模型带工具（查订单、查库存、发起审批草稿），RAG 成为 Agent 的知识工具之一
10. **审批/风控场景**：合同条款比对、报销单据与制度自动核对（结构化规则 + 语义检索混合）
11. **多模态**：发票图片识别入报销链路、扫描件合同入库
12. **推理基建**：GPU 推理节点或弹性 API 混合调度；按模型/租户的用量成本看板

## 六、运维备忘（生产实况）

- Qdrant v1.10.1（**最新版需 GLIBC 2.38，服务器 22.04 仅 2.35，勿盲目升级二进制**），systemd `qdrant`，数据 `/data/qdrant`，仅监听 127.0.0.1
- 表：`ai_knowledge_base` / `ai_knowledge_document`（`sql/mysql/ai_rag.sql`）；菜单 791/850-854 已启用（`ai_menu_enable.sql`）
- 开关 `ENTERPRISE_AI_RAG_ENABLED` 生产当前 **false**；开启前置条件：模型 API 就绪（`initialize-schema` 启动即调嵌入探维度，模型不在线会启动失败）
- 冒烟口径：建库 → 传 `docs/rag-test-corpus` 语料 → 问"销售退货流程"验证引用来源 → 换租户验证空结果隔离
