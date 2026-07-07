# T-035b 智能问答UI与对话管理

**版本**：v1.0.0  
**模块**：`app-ui`  
**预估**：1 天  
**前置**：T-035a  
**状态**：待开始

---

## 任务描述

实现智能问答的聊天 UI 界面和对话历史管理。

## 详细步骤

### 1. 聊天页面布局

```
┌────────────────────────────────────────────────┐
│  智能问答                          [清空对话] [⚙]│
├────────────────────────────────────────────────┤
│                                                │
│  ┌──────────────────────────────────────────┐  │
│  │ 你好！我是 POE 智能助手，有什么可以帮你的？  │  │
│  │ 试试问我：                                  │  │
│  │ [法师之血效果] [龙卷射击连法] [希鲁斯攻略]    │  │
│  └──────────────────────────────────────────┘  │
│                                                │
│                        ┌───────────────────┐   │
│                        │ 法师之血有什么效果？│   │
│                        └───────────────────┘   │
│                                                │
│  ┌──────────────────────────────────────────┐  │
│  │ 法师之血(Mageblood)是一件传奇重革腰带：    │  │
│  │                                          │  │
│  │ 固定词缀：+(15-25) 全属性                  │  │
│  │                                          │  │
│  │ 传奇词缀：                                │  │
│  │ • 魔法功能药剂效果持续期间，其效果不会移除   │  │
│  │ • 魔法功能药剂至少有4个生效效果时，         │  │
│  │   增加(5-10)%所有属性                     │  │
│  │                                          │  │
│  │ 💡 简单说：药水永续，不用再按了！           │  │
│  │                                          │  │
│  │ 📚 来源: PoE Wiki | 数据版本: 3.24        │  │
│  └──────────────────────────────────────────┘  │
│                                                │
│  ┌──────────────────────────────────────────┐  │
│  │ [输入问题...]                    [发送 ➤]  │  │
│  └──────────────────────────────────────────┘  │
└────────────────────────────────────────────────┘
```

### 2. 消息气泡组件

```java
public class ChatBubble extends HBox {
    public ChatBubble(ChatMessage message) {
        // 用户消息：右对齐，蓝色气泡
        // AI 消息：左对齐，暗色气泡
        // 显示时间戳
        // AI 消息显示引用来源链接
    }
}
```

### 3. 流式输出

```java
// AI 回复逐字显示
ragService.askStream(question,
    chunk -> {
        // 追加到当前 AI 气泡
        Platform.runLater(() -> currentBubble.appendText(chunk));
    },
    error -> Platform.runLater(() -> showError(error)),
    () -> Platform.runLater(() -> {
        currentBubble.finalize();     // 完成渲染
        scrollToBottom();             // 滚动到底部
    })
);
```

### 4. 对话管理

```java
public class ConversationManager {
    private List<Conversation> conversations;
    private Conversation currentConversation;

    /** 创建新对话 */
    public Conversation newConversation() { ... }

    /** 保存对话到 SQLite */
    public void save(Conversation conv) { ... }

    /** 加载历史对话列表 */
    public List<ConversationSummary> listConversations() { ... }

    /** 加载指定对话 */
    public Conversation loadConversation(String id) { ... }

    /** 删除对话 */
    public void deleteConversation(String id) { ... }
}
```

### 5. Conversation 模型

```java
class Conversation {
    String id;
    String title;                  // 自动生成（第一条消息截取）
    List<ChatMessage> messages;
    Instant createdAt;
    Instant updatedAt;
}

class ChatMessage {
    MessageRole role;              // USER / ASSISTANT / SYSTEM
    String content;
    List<SourceReference> sources;  // 仅 ASSISTANT 有
    Instant timestamp;
}
```

### 6. 快捷功能

- [ ] 常用问题快捷按钮（预设 5-8 个高频问题）
- [ ] 对话历史侧边栏（可折叠）
- [ ] 清空当前对话
- [ ] 复制回答内容
- [ ] 对话导出为 Markdown

## 验收标准

- [ ] 消息气泡样式美观
- [ ] 流式输出逐字显示
- [ ] 对话历史持久化（重启不丢失）
- [ ] 多轮对话上下文保持
- [ ] 对话列表管理（新建/切换/删除）
- [ ] 引用来源可点击跳转
