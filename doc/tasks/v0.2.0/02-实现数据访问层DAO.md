# T-010 实现数据访问层 (DAO)

**版本**：v0.2.0  
**模块**：`data-cache`  
**预估**：0.5 天  
**前置**：T-009  
**状态**：已完成

---

## 任务描述

实现 DatabaseManager 和所有表的 DAO 层，提供统一的数据访问接口。

## 详细步骤

### 1. DatabaseManager

```java
public class DatabaseManager {
    private static final String DB_PATH = 
        System.getProperty("user.home") + "/.poe-tool/data/poe.db";
    
    private static DatabaseManager instance;

    public static DatabaseManager getInstance() { ... }

    public Connection getConnection() throws SQLException { ... }
    public void init() { /* 执行迁移脚本 */ }
    public void close() { ... }
}
```

### 2. DAO 接口与实现

```java
// 通用接口
public interface CrudRepository<T, ID> {
    void insert(T entity);
    void batchInsert(List<T> entities);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
    int count();
}

// 具体实现
ItemDao implements CrudRepository<Item, Integer>
SkillGemDao implements CrudRepository<SkillGem, Integer>
PassiveSkillDao implements CrudRepository<PassiveSkill, Integer>
ModDao implements CrudRepository<Mod, Integer>
TranslationDao {
    Optional<String> translate(String source, String domain);
    Map<String, String> batchTranslate(List<String> sources, String domain);
    void saveTranslation(String source, String target, String domain);
    void batchSave(Map<String, String> translations, String domain);
}
```

### 3. 搜索 DAO

```java
// FTS 全文搜索
public interface SearchDao {
    List<ItemSummary> searchItems(String keyword, int limit, int offset);
    int searchItemCount(String keyword);
}

// 实现中使用 FTS5 查询
// SELECT * FROM items_fts WHERE items_fts MATCH ? ORDER BY rank LIMIT ? OFFSET ?
```

## 验收标准

- [ ] 每个 DAO 有单元测试（使用内存 SQLite）
- [ ] CRUD 操作全部通过
- [ ] FTS 搜索返回正确结果
- [ ] 批量插入 1000 条数据 < 1s
