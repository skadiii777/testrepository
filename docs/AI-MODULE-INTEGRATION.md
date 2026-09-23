# AI 能力接入约定

业务模块可依赖 `enterprise-common` 中的 `AiKnowledgeAssistant` 接口调用知识库问答，不应直接依赖 `enterprise-module-ai` 的实现类。接口接收知识库 ID 和问题，返回回答及引用来源；实现会使用当前登录租户上下文，并校验知识库归属。

```java
import com.enterprise.framework.ai.api.AiKnowledgeAssistant;
import org.springframework.beans.factory.ObjectProvider;

public class SalesHelpService {

    private final ObjectProvider<AiKnowledgeAssistant> assistantProvider;

    public SalesHelpService(ObjectProvider<AiKnowledgeAssistant> assistantProvider) {
        this.assistantProvider = assistantProvider;
    }

    public AiKnowledgeAssistant.Answer ask(Long knowledgeBaseId, String question) {
        AiKnowledgeAssistant assistant = assistantProvider.getIfAvailable();
        if (assistant == null) {
            throw new IllegalStateException("AI 知识问答未启用");
        }
        return assistant.answer(knowledgeBaseId, question);
    }
}
```

RAG 默认关闭。未启用时接口实现不会注册，业务模块可用 `ObjectProvider` 保持应用正常启动，并在调用时给出清晰提示。当前问答入口要求业务明确提供知识库 ID，避免隐式跨库检索；底层按租户和知识库双重过滤。

前端现有 `src/api/ai/knowledge` 导出 `askKnowledge(knowledgeBaseId, question)`，可供后续业务页面复用同一问答接口和引用结构。
