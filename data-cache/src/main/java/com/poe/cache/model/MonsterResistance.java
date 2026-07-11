package com.poe.cache.model;

/**
 * 怪物抗性配置实体，映射 monster_resistances 表。
 * <p>
 * 存储怪物在剧情第一部、第二部及异界地图中的各元素/混沌抗性。
 * 主键为 page_id，resistanceId 对应 Cargo 的 id 字段。
 */
public class MonsterResistance {

    /** Wiki 页面 ID（主键） */
    private Integer pageId;
    /** Wiki 页面名称（_pageName） */
    private String pageName;
    /** 抗性配置 ID */
    private String resistanceId;
    /** 异界地图混沌抗性 */
    private int mapsChaos;
    /** 异界地图冰霜抗性 */
    private int mapsCold;
    /** 异界地图火焰抗性 */
    private int mapsFire;
    /** 异界地图闪电抗性 */
    private int mapsLightning;
    /** 第一部混沌抗性 */
    private int part1Chaos;
    /** 第一部冰霜抗性 */
    private int part1Cold;
    /** 第一部火焰抗性 */
    private int part1Fire;
    /** 第一部闪电抗性 */
    private int part1Lightning;
    /** 第二部混沌抗性 */
    private int part2Chaos;
    /** 第二部冰霜抗性 */
    private int part2Cold;
    /** 第二部火焰抗性 */
    private int part2Fire;
    /** 第二部闪电抗性 */
    private int part2Lightning;

    public MonsterResistance() {}

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getResistanceId() { return resistanceId; }
    public void setResistanceId(String resistanceId) { this.resistanceId = resistanceId; }

    public int getMapsChaos() { return mapsChaos; }
    public void setMapsChaos(int mapsChaos) { this.mapsChaos = mapsChaos; }

    public int getMapsCold() { return mapsCold; }
    public void setMapsCold(int mapsCold) { this.mapsCold = mapsCold; }

    public int getMapsFire() { return mapsFire; }
    public void setMapsFire(int mapsFire) { this.mapsFire = mapsFire; }

    public int getMapsLightning() { return mapsLightning; }
    public void setMapsLightning(int mapsLightning) { this.mapsLightning = mapsLightning; }

    public int getPart1Chaos() { return part1Chaos; }
    public void setPart1Chaos(int part1Chaos) { this.part1Chaos = part1Chaos; }

    public int getPart1Cold() { return part1Cold; }
    public void setPart1Cold(int part1Cold) { this.part1Cold = part1Cold; }

    public int getPart1Fire() { return part1Fire; }
    public void setPart1Fire(int part1Fire) { this.part1Fire = part1Fire; }

    public int getPart1Lightning() { return part1Lightning; }
    public void setPart1Lightning(int part1Lightning) { this.part1Lightning = part1Lightning; }

    public int getPart2Chaos() { return part2Chaos; }
    public void setPart2Chaos(int part2Chaos) { this.part2Chaos = part2Chaos; }

    public int getPart2Cold() { return part2Cold; }
    public void setPart2Cold(int part2Cold) { this.part2Cold = part2Cold; }

    public int getPart2Fire() { return part2Fire; }
    public void setPart2Fire(int part2Fire) { this.part2Fire = part2Fire; }

    public int getPart2Lightning() { return part2Lightning; }
    public void setPart2Lightning(int part2Lightning) { this.part2Lightning = part2Lightning; }
}
