# T-002 搭建 common 模块

**版本**：v0.1.0  
**模块**：`common`  
**预估**：0.5 天  
**前置**：T-001  
**状态**：已完成

---

## 任务描述

搭建公共基础模块，提供字符串工具、JSON 工具、基础异常类等所有模块都会用到的公共代码。

## 详细步骤

### 1. 创建 `common/build.gradle.kts`

```kotlin
plugins {
    java
}

dependencies {
    // Jackson JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    
    // SLF4J API (实现由各模块自行引入)
    implementation("org.slf4j:slf4j-api:2.0.9")
}
```

### 2. 实现 `StringUtils`

```java
package com.poe.common.util;

public final class StringUtils {

    private StringUtils() {}

    public static boolean isEmpty(String s) { ... }
    public static boolean isBlank(String s) { ... }
    
    /** 标准化物品名：去空格、统一小写，用于模糊匹配 */
    public static String normalizeName(String name) { ... }
}
```

### 3. 实现 `JsonUtils`

```java
package com.poe.common.util;

public final class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object obj) { ... }
    public static <T> T fromJson(String json, Class<T> clazz) { ... }
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) { ... }
}
```

### 4. 定义基础异常类

```
com.poe.common.exception
├── PoeException.java          // 基础异常
├── DataSyncException.java     // 数据同步异常
└── ConfigException.java       // 配置异常
```

## 验收标准

- [ ] `./gradlew :common:build` 构建成功
- [ ] 所有工具类有单元测试
- [ ] `StringUtils.normalizeName("Mage Blood")` → `"mageblood"`
- [ ] `JsonUtils` 序列化/反序列化正常
