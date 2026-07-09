# T-012 实现 Wiki API 客户端

**版本**：v0.2.0  
**模块**：`data-provider`（本次新建）  
**预估**：1 天  
**前置**：T-002  
**状态**：待开始

---

## 任务描述

创建 data-provider 模块，实现 PoE Wiki Cargo 数据 API 的 HTTP 客户端。

## 详细步骤

### 1. 创建 `data-provider/build.gradle.kts`

```kotlin
dependencies {
    implementation(project(":common"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

### 2. WikiApiClient

```java
public class WikiApiClient {
    private static final String WIKI_API = 
        "https://www.poewiki.net/w/api.php";
    
    private final OkHttpClient httpClient;
    private final RateLimiter rateLimiter;  // 每秒最多 5 次

    /**
     * 查询 Cargo 表数据
     * @param tableName 表名，如 "items"
     * @param fields 需要的字段，如 "_pageName,name,class"
     * @param offset 偏移量
     * @param limit 每批数量（最大 500）
     */
    public JsonNode queryCargoTable(
            String tableName, String fields, 
            int offset, int limit) { ... }

    /**
     * 获取表总记录数
     */
    public int queryCargoTableCount(String tableName) { ... }

    /**
     * 获取 Wiki 页面原始 wikitext
     */
    public String queryPageContent(String pageTitle) { ... }
}
```

### 3. 重试与限流

```java
// 重试策略：最多 3 次，指数退避
// 1st retry: wait 1s, 2nd: wait 2s, 3rd: wait 4s

// 限流：Guava RateLimiter
RateLimiter limiter = RateLimiter.create(5.0);  // 5 requests/sec
limiter.acquire();  // 请求前获取许可
```

### 4. 核心 Cargo 表映射

| 表名 | 用途 | 预估记录数 |
|------|------|-----------|
| `items` | 所有物品基础信息 | ~5000+ |
| `skill_gems` | 技能和辅助宝石 | ~600+ |
| `passive_skills` | 天赋树节点 | ~1500+ |
| `mods` | 所有词缀 | ~10000+ |
| `base_items` | 装备基底 | ~1500+ |
| `crafting_bench_options` | 工艺台选项 | ~500+ |

## 验收标准

- [ ] 成功获取 `items` 表前 100 条数据
- [ ] 解析 MediaWiki API 嵌套 JSON 结构
- [ ] 请求限流生效（不超过 5 req/s）
- [ ] 失败自动重试
- [ ] 网络超时有明确错误提示
