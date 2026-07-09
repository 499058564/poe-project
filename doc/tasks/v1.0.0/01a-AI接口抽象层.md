# T-032a AI 接口抽象层

**版本**：v1.0.0  
**模块**：`app-core`  
**预估**：1 天  
**前置**：无  
**状态**：待开始

---

## 任务描述

抽象 AI 大模型调用接口，实现 OpenAI 和 Ollama（本地）两种 Provider，支持配置切换。

## 详细步骤

### 1. AiProvider 接口

```java
public interface AiProvider {

    /** 发送对话请求（阻塞） */
    String chat(String systemPrompt, String userMessage);

    /** 流式对话（逐 chunk 回调） */
    void chatStream(String systemPrompt, String userMessage, 
                    Consumer<String> onChunk, 
                    Consumer<Throwable> onError,
                    Runnable onComplete);

    /** 获取模型名称 */
    String getModelName();
}
```

### 2. AiConfig — AI 配置

```java
public class AiConfig {
    private String provider = "openai";     // "openai" / "ollama" / "custom"
    private String apiKey = "";
    private String apiUrl = "https://api.openai.com/v1/chat/completions";
    private String model = "gpt-4o";
    private int maxTokens = 4096;
    private double temperature = 0.7;
    
    // 从 AppConfig 读取
    public static AiConfig fromAppConfig(AppConfig config) { ... }
}
```

### 3. OpenAiProvider

```java
public class OpenAiProvider implements AiProvider {
    private final OkHttpClient httpClient;
    private final AiConfig config;

    @Override
    public String chat(String systemPrompt, String userMessage) {
        // POST https://api.openai.com/v1/chat/completions
        // Authorization: Bearer {apiKey}
        // Body: { model, messages: [{role:system, content}, {role:user, content}] }
        // 返回: choices[0].message.content
    }

    @Override
    public void chatStream(String systemPrompt, String userMessage, 
                           Consumer<String> onChunk, ...) {
        // 同上，但 stream: true
        // 解析 SSE (Server-Sent Events) 格式
        // 每个 data: {...} 为一个 chunk
    }
}
```

### 4. OllamaProvider（本地模型）

```java
public class OllamaProvider implements AiProvider {
    private static final String DEFAULT_URL = "http://localhost:11434/api/chat";

    @Override
    public String chat(String systemPrompt, String userMessage) {
        // POST http://localhost:11434/api/chat
        // Body: { model: "qwen2.5:14b", messages: [...], stream: false }
    }
    
    // 流式类似，stream: true，每行一个 JSON chunk
}
```

### 5. AiProviderFactory

```java
public class AiProviderFactory {
    public static AiProvider create(AiConfig config) {
        return switch (config.getProvider()) {
            case "openai" -> new OpenAiProvider(config);
            case "ollama" -> new OllamaProvider(config);
            default -> throw new IllegalArgumentException("不支持的 AI 提供商: " + config.getProvider());
        };
    }
}
```

### 6. 离线降级

```java
// 未配置 API Key 或无法连接 → 提示用户在设置中配置
// Ollama 未运行 → 提示启动 Ollama 服务
// 网络错误 → 重试 3 次后提示
```

## 验收标准

- [ ] OpenAI API 调用成功，返回正确回复
- [ ] Ollama 本地模型调用成功
- [ ] 流式输出 chunk 回调正常
- [ ] 通过配置文件切换 Provider
- [ ] 未配置时给出明确提示
- [ ] 网络错误重试 + 友好提示
