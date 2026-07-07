# T-024b LuaBridge 高级封装

**版本**：v0.4.0  
**模块**：`pob-ipc`  
**预估**：0.75 天  
**前置**：T-024a, T-023  
**状态**：待开始

---

## 任务描述

在进程通信基础上封装 LuaBridge，提供面向业务的 Build 计算接口。

## 详细步骤

### 1. LuaBridge

```java
public class LuaBridge {
    private final PobProcessManager processManager;
    private final JsonProtocol protocol;
    private final BuildParser buildParser;

    /** 计算 Build 全部数据 */
    public CalculationResult calculate(Build build) {
        String buildXml = buildParser.serialize(build);
        JsonNode payload = mapper.createObjectNode()
            .put("build", buildXml);

        JsonNode response = protocol.sendRequest("calculate", payload)
            .get(30, TimeUnit.SECONDS);

        return parseCalculationResult(response);
    }

    /** 验证 Build 合法性 */
    public List<String> validateBuild(Build build) {
        // 发送 validate 请求，返回警告/错误列表
    }

    /** 获取支持的 POB 版本 */
    public List<String> listSupportedVersions() {
        // 发送 version 请求
    }

    /** 获取物品计算结果（单件装备变化后的 DPS 差异） */
    public ItemCompareResult compareItem(Build build, ItemSlot oldItem, ItemSlot newItem) {
        // 替换装备后重新计算，对比差异
    }

    /** 批量计算（多个 Build 版本对比） */
    public List<CalculationResult> batchCalculate(List<Build> builds) {
        // 利用进程池复用，并行计算
    }
}
```

### 2. 进程池管理

```java
public class ProcessPool {
    private final int poolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    private final Queue<PobProcessManager> available = new ConcurrentLinkedQueue<>();

    /** 获取空闲进程（无可用时阻塞等待） */
    public PobProcessManager acquire() { ... }

    /** 归还进程 */
    public void release(PobProcessManager pm) { ... }

    /** 关闭所有进程 */
    public void shutdownAll() { ... }
}
```

### 3. 计算结果解析

```java
private CalculationResult parseCalculationResult(JsonNode response) {
    JsonNode data = response.get("data");

    CalculationResult result = new CalculationResult();
    result.setTotalDps(parseStatLine(data.get("totalDps")));
    result.setLife(parseStatLine(data.get("life")));
    result.setResistances(parseResistances(data.get("resistances")));
    // ... 解析所有统计字段
    return result;
}
```

### 4. 错误处理

```java
// POB 计算错误 → CalculationException
// 进程崩溃 → ProcessDiedException（自动重启）
// 版本不兼容 → VersionMismatchException
// 超时 → TimeoutException
```

## 验收标准

- [ ] Java 发送 Build XML → 返回结构化 CalculationResult
- [ ] Build 验证返回错误/警告列表
- [ ] 单装备替换后正确计算 DPS 差异
- [ ] 进程崩溃后自动重启，重试当前请求
- [ ] 并发计算使用进程池不阻塞
