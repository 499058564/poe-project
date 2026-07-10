package com.poe.cache.model;

/**
 * 基础物品实体，映射 base_items 表。
 * <p>
 * {@code requirements}、{@code implicits}、{@code properties} 字段
 * 以 JSON 字符串形式存储数组/对象结构，由上层服务负责序列化/反序列化。
 */
public class Item {
    /** PoE 内部物品 ID，主键 */
    private Integer id;
    /** 英文名称 */
    private String name;
    /** 中文译名，可能为 null */
    private String nameZh;
    /** 物品类别，如 "Helmet"、"Belt"、"Ring" */
    private String itemClass;
    /** 背包占宽（格数） */
    private int inventoryWidth;
    /** 背包占高（格数） */
    private int inventoryHeight;
    /** JSON: 属性需求，如 [{name:"str", values:[["100"]]}] */
    private String requirements;
    /** JSON: 基底词缀，如 [{text:"+20 to maximum Life"}] */
    private String implicits;
    /** JSON: 固定属性，如 [{name:"Armour", values:[["100"]], displayMode:0}] */
    private String properties;
    /** 装备背景文字 */
    private String flavourText;
    /** 最低掉落等级 */
    private int dropLevel;
    /** PoE Wiki 页面 URL */
    private String wikiUrl;
    /** 数据所属游戏版本号，如 "3.24" */
    private String version;

    public Item() {}

    public Item(Integer id, String name, String itemClass, String version) {
        this.id = id;
        this.name = name;
        this.itemClass = itemClass;
        this.version = version;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }

    public String getItemClass() { return itemClass; }
    public void setItemClass(String itemClass) { this.itemClass = itemClass; }

    public int getInventoryWidth() { return inventoryWidth; }
    public void setInventoryWidth(int inventoryWidth) { this.inventoryWidth = inventoryWidth; }

    public int getInventoryHeight() { return inventoryHeight; }
    public void setInventoryHeight(int inventoryHeight) { this.inventoryHeight = inventoryHeight; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getImplicits() { return implicits; }
    public void setImplicits(String implicits) { this.implicits = implicits; }

    public String getProperties() { return properties; }
    public void setProperties(String properties) { this.properties = properties; }

    public String getFlavourText() { return flavourText; }
    public void setFlavourText(String flavourText) { this.flavourText = flavourText; }

    public int getDropLevel() { return dropLevel; }
    public void setDropLevel(int dropLevel) { this.dropLevel = dropLevel; }

    public String getWikiUrl() { return wikiUrl; }
    public void setWikiUrl(String wikiUrl) { this.wikiUrl = wikiUrl; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
