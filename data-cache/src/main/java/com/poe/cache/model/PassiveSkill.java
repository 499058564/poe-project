package com.poe.cache.model;

/**
 * 天赋点实体，映射 passive_skills 表。
 * <p>
 * {@code stats} 和 {@code connections} 以 JSON 字符串存储。
 * 布尔字段对应 SQLite 中 INTEGER(0/1)。
 */
public class PassiveSkill {
    /** PoE 内部天赋 ID，主键 */
    private Integer id;
    /** 英文名称 */
    private String name;
    /** 中文译名，可能为 null */
    private String nameZh;
    /** 天赋类别，如 "Normal"、"Ascendancy" */
    private String passiveClass;
    /** 升华职业名称，普通天赋为 null */
    private String ascendancy;
    /** JSON: 天赋效果，如 [{id:"maximum_life_+%", value:6}] */
    private String stats;
    /** 是否为核心天赋（Keystone） */
    private boolean keystone;
    /** 是否为显著天赋（Notable） */
    private boolean notable;
    /** 是否为珠宝插槽 */
    private boolean jewelSocket;
    /** 天赋树 X 坐标 */
    private double x;
    /** 天赋树 Y 坐标 */
    private double y;
    /** JSON: 相邻天赋 ID 数组，如 [1234, 5678] */
    private String connections;
    /** 数据所属游戏版本号，如 "3.24" */
    private String version;

    public PassiveSkill() {}

    public PassiveSkill(Integer id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }

    public String getPassiveClass() { return passiveClass; }
    public void setPassiveClass(String passiveClass) { this.passiveClass = passiveClass; }

    public String getAscendancy() { return ascendancy; }
    public void setAscendancy(String ascendancy) { this.ascendancy = ascendancy; }

    public String getStats() { return stats; }
    public void setStats(String stats) { this.stats = stats; }

    public boolean isKeystone() { return keystone; }
    public void setKeystone(boolean keystone) { this.keystone = keystone; }

    public boolean isNotable() { return notable; }
    public void setNotable(boolean notable) { this.notable = notable; }

    public boolean isJewelSocket() { return jewelSocket; }
    public void setJewelSocket(boolean jewelSocket) { this.jewelSocket = jewelSocket; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public String getConnections() { return connections; }
    public void setConnections(String connections) { this.connections = connections; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
