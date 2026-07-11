package com.poe.cache.model;

/**
 * FTS 搜索结果摘要，仅包含列表展示所需的字段。
 * <p>
 * rank 为 FTS5 相关性评分，值越低表示匹配度越高。
 */
public class ItemSummary {
    /** 物品 ID */
    private int id;
    /** 英文名称 */
    private String name;
    /** 中文译名 */
    private String nameZh;
    /** 物品类别 */
    private String itemClass;
    /** 最低掉落等级 */
    private int dropLevel;
    /** Wiki 图标 URL（如有） */
    private String iconUrl;
    /** FTS5 相关性评分，值越低匹配度越高 */
    private double rank;

    public ItemSummary() {}

    public ItemSummary(int id, String name, String nameZh, String itemClass, double rank) {
        this.id = id;
        this.name = name;
        this.nameZh = nameZh;
        this.itemClass = itemClass;
        this.rank = rank;
    }

    public ItemSummary(int id, String name, String nameZh, String itemClass, int dropLevel, String iconUrl, double rank) {
        this.id = id;
        this.name = name;
        this.nameZh = nameZh;
        this.itemClass = itemClass;
        this.dropLevel = dropLevel;
        this.iconUrl = iconUrl;
        this.rank = rank;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }

    public String getItemClass() { return itemClass; }
    public void setItemClass(String itemClass) { this.itemClass = itemClass; }

    public int getDropLevel() { return dropLevel; }
    public void setDropLevel(int dropLevel) { this.dropLevel = dropLevel; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public double getRank() { return rank; }
    public void setRank(double rank) { this.rank = rank; }
}
