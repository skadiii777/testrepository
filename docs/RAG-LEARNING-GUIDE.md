# RAG 与智能体学习指南

> 基于 enterprise-module-ai 模块的实际代码，逐步理解 RAG 和智能体的核心概念。

---

## 第一步：理解嵌入（Embedding）

**一句话**：把文字变成一串数字（向量），语义相近的文字，向量距离也近。

```
"差旅报销标准" → [0.12, -0.34, 0.56, ..., 0.78]  ← 一个 768 维向量
"出差费用报销" → [0.11, -0.30, 0.58, ..., 0.75]  ← 和上面很近（语义相似）
"量子物理原理" → [-0.67, 0.23, -0.11, ..., 0.03]  ← 和上面很远（语义无关）
```

**在你的代码里**：
```yaml
# application.yaml
spring.ai.ollama.embedding.options.model: nomic-embed-text
```
Ollama 的 `nomic-embed-text` 模型负责把中文/英文转成向量。你调用 `vectorStore.add(docs)` 时，Spring AI 自动对每条文本调用这个模型算出向量。

**学习要点**：嵌入模型不理解"意思"，它只是学会了"哪些文字经常一起出现"。所以同义词距离近，不同话题距离远。

---

## 第二步：理解向量检索

**一句话**：拿用户问题的向量，在向量库里找距离最近的 K 条文本。

```
用户问："去北京出差住宿标准是多少？"
      ↓ 嵌入
问题向量 [0.15, -0.28, ...]
      ↓ 和库里的每条向量算余弦相似度
匹配到："住宿费用：一线城市每晚不超过500元..."  ← 相似度 0.89（最高）
匹配到："交通费用：高铁二等座实报实销..."      ← 相似度 0.42（较低）
```

**在你的代码里**：
```java
// RagService.java → chat() 方法
List<Document> relevant = vectorStore.similaritySearch(
    SearchRequest.builder().query(question).topK(3).build()
);
```
Spring AI 自动做了：问题 → 嵌入 → 余弦相似度排序 → 返回 TOP_K 条。

**学习要点**：`topK=3` 表示取最相似的 3 条。太大（如 10）会引入无关信息干扰回答；太小（如 1）可能漏掉关键上下文。

---

## 第三步：理解 RAG（检索增强生成）

**一句话**：RAG = 检索（Retrieve）+ 增强（Augment）+ 生成（Generate）。

```
完整流程：
用户问"住宿标准是多少"
    ↓
① 检索：在向量库里找 3 条最相关的文本片段
    ↓
② 增强：把片段拼进提示词
    "参考信息：
     一、住宿费用：一线城市每晚不超过500元...
     --- 
     二、餐补：每天100元..."
    ↓
③ 生成：LLM 根据这些片段回答，而不是凭"记忆"瞎编
```

**在你的代码里**：
```java
// RagService.java → chat() 方法
String context = relevant.stream()
        .map(Document::getText)
        .collect(Collectors.joining("\n---\n"));

return chatClient.prompt()
        .user(u -> u.text(RAG_PROMPT)
                .param("context", context)
                .param("question", question))
        .call()
        .content();
```

**为什么要 RAG 而不是直接问 LLM**：
- LLM 的训练数据有截止日期，不知道你公司的制度
- LLM 会"幻觉"（编造合理但错误的答案）
- RAG 让 LLM **只根据你给的文档回答**，有据可查

**学习要点**：提示词里的指令"如果参考信息中没有相关内容，请如实说"很关键——不加这句，小模型可能用自己的知识回答（如量子物理问题），而不是承认不知道。

---

## 第四步：理解分块（Chunking）

**一句话**：把长文档切成小段，每段独立嵌入，这样检索才能精确匹配到相关段落。

```
一篇 2000 字的制度文档
    ↓ 按双换行拆段
[段落1] 交通费用：高铁二等座...     ← 80 字
[段落2] 住宿费用：一线城市...       ← 30 字  
[段落3] 餐补：每天100元...          ← 15 字
[段落4] 报销流程：差旅结束后...     ← 40 字
```

**在你的代码里**：
```java
// RagService.java → chunkText() 方法
for (String para : text.split("\n\n")) { ... }
```

**学习要点**：
- 块太大（1000 字+）→ 检索不准（一段里混了多个话题）
- 块太小（50 字以下）→ 上下文不够（LLM 理解不了孤立的一句话）
- 500 字左右是中文文本的甜蜜点

---

## 第五步：理解智能体（Agent）与工具调用

**一句话**：Agent = LLM + 工具集。LLM 判断用户意图，决定调用哪个工具。

```
用户："上个月卖了多少？"
    ↓ LLM 分析
"这需要查销售数据，我应该调用 getSalesStats 工具"
    ↓ 执行工具
工具返回：{ "totalAmount": 50000, "orderCount": 15 }
    ↓ LLM 拿到数字，组织自然语言回答
"上个月共有 15 笔销售订单，总金额 50,000 元。"
```

**Spring AI 的实现方式**（下一阶段实现）：
```java
// 把业务方法注册为"工具"
@Tool(description = "查询指定日期范围的销售总额和订单数")
public SalesStats getSalesStats(@ToolParam("开始日期") String start,
                                 @ToolParam("结束日期") String end) {
    return salesMapper.selectStatsByDate(start, end);
}
```
LLM 看到工具描述后，自动决定何时调用、传什么参数。

**学习要点**：
- 工具描述（description）极其重要——LLM 靠它判断什么时候该调这个工具
- 工具越多（>10 个），LLM 选择越容易出错——控制在 5-8 个以内
- 工具的参数也要有清晰描述，否则 LLM 传参会出错

---

## 第六步：理解向量库选型

| 向量库 | 特点 | 适用 |
|---|---|---|
| **SimpleVectorStore**（你当前用的） | 内存 + JSON 文件，零依赖 | 学习/原型 |
| **Chroma** | 单进程，Python 生态，有 Web UI | 中小型项目 |
| **pgvector** | PostgreSQL 扩展，和关系数据同库 | 已有 PG 的项目 |
| **Milvus** | 分布式，支持十亿级向量 | 大规模生产 |
| **Redis** | 已有 Redis 加 RediSearch 模块 | 复用现有 Redis |

**你当前用的 SimpleVectorStore 的局限**：
- 所有向量在内存里（万级文档没问题，十万级会卡）
- 重启需要从 JSON 文件恢复（文件大了加载慢）
- 不支持增量删除单条（只能全量重建）

**换正式向量库时**：只需换 `VectorStoreConfig` 里的 Bean 实现，`RagService` 上层代码零改动——这就是面向接口编程的好处。

---

## 核心代码架构图

```
enterprise-module-ai/
│
├── config/VectorStoreConfig.java     ← 向量库 Bean（SimpleVectorStore）
│
├── service/RagService.java           ← 核心：ingest() + chat()
│   ├── ingest(text, docName)          文本 → chunkText() → vectorStore.add()
│   └── chat(question)                 问题 → similaritySearch() → ChatClient → 回答
│
└── controller/admin/RagController.java ← HTTP 接口
    ├── POST /ingest                    知识入库
    └── POST /chat                      RAG 问答

application.yaml
└── spring.ai.ollama.base-url + model 配置

外部依赖
├── Ollama (11434 端口) ← 聊天模型 + 嵌入模型
└── vector-store.json   ← 持久化文件
```

---

## 常见问题

| 问题 | 原因 | 解决 |
|---|---|---|
| 回答不含知识库内容 | 小模型不遵循提示词 | 换 7B+ 模型，或加强提示词约束 |
| 检索到无关内容 | 分块太大/太碎 | 调整分块大小（300-500 字） |
| 相似内容没被检索到 | 嵌入模型对中文不敏感 | 换 bge-m3 等中文优化嵌入模型 |
| 重启后知识丢失 | 检查 Qdrant 是否为持久化部署、集合名是否一致及服务端连接配置 |
| 回答太慢 | 1.5B 模型在 CPU 上推理 | 换 API 或 GPU |
# 当前首期实现说明（2026-09-23）

首期已接入 Ollama 聊天/嵌入模型与 Qdrant 持久向量库，并提供知识库创建、TXT/Markdown 上传、文档查询和删除、带来源引用的问答。MySQL 中的知识库和文档元数据按服务端当前租户隔离，向量检索同时过滤租户 ID 与知识库 ID。前端菜单复用现有 `AI 知识库` 菜单项（组件路径 `ai/knowledge/knowledge/index`）。

启用步骤：

1. 先在目标环境人工执行 `sql/mysql/ai_rag.sql`，确保应用账号具备两张新表的读写权限。
2. 准备 Ollama 的聊天模型和 `nomic-embed-text` 嵌入模型，并启动 Qdrant gRPC 服务。
3. 设置 `ENTERPRISE_AI_RAG_ENABLED=true`、`ENTERPRISE_AI_RAG_QDRANT_HOST` 和端口；云端 Qdrant 使用 TLS 与 API Key 环境变量。
4. 为相应角色授予现有 `ai:knowledge:create/query/delete` 菜单权限，再重启应用并联调上传、引用问答、删除。

首期上传只接受 UTF-8 `.txt`/`.md`，文件最大 2 MiB、文本最多 20 万字符。PDF/Office 解析、异步大文件入库、重试队列、混合检索、评测集和审计记录留待后续阶段。RAG 的 MySQL 与 Qdrant 写入目前通过应用层补偿清理，不具备跨库原子事务保证。
