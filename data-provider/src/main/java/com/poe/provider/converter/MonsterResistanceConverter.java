package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.MonsterResistance;

/**
 * 将 Wiki Cargo {@code monster_resistances} 表行转换为 {@link MonsterResistance} 模型。
 * <p>
 * Cargo 字段：id, maps_chaos, maps_cold, maps_fire, maps_lightning,
 * part1_chaos, part1_cold, part1_fire, part1_lightning,
 * part2_chaos, part2_cold, part2_fire, part2_lightning。
 * Cargo 的 id 字段映射为 resistanceId。
 */
public class MonsterResistanceConverter implements DataConverter<MonsterResistance> {

    /**
     * 将 Cargo 单行 JSON 转换为 MonsterResistance 实体。
     *
     * @param row Cargo 返回的 title 节点
     * @return 怪物抗性实体，字段缺失时使用默认值
     */
    @Override
    public MonsterResistance convert(JsonNode row) {
        MonsterResistance mr = new MonsterResistance();
        mr.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        mr.setPageName(row.path("_pageName").asText());
        mr.setResistanceId(row.path("id").asText());
        mr.setMapsChaos(ItemConverter.parseIntSafe(row, "maps_chaos"));
        mr.setMapsCold(ItemConverter.parseIntSafe(row, "maps_cold"));
        mr.setMapsFire(ItemConverter.parseIntSafe(row, "maps_fire"));
        mr.setMapsLightning(ItemConverter.parseIntSafe(row, "maps_lightning"));
        mr.setPart1Chaos(ItemConverter.parseIntSafe(row, "part1_chaos"));
        mr.setPart1Cold(ItemConverter.parseIntSafe(row, "part1_cold"));
        mr.setPart1Fire(ItemConverter.parseIntSafe(row, "part1_fire"));
        mr.setPart1Lightning(ItemConverter.parseIntSafe(row, "part1_lightning"));
        mr.setPart2Chaos(ItemConverter.parseIntSafe(row, "part2_chaos"));
        mr.setPart2Cold(ItemConverter.parseIntSafe(row, "part2_cold"));
        mr.setPart2Fire(ItemConverter.parseIntSafe(row, "part2_fire"));
        mr.setPart2Lightning(ItemConverter.parseIntSafe(row, "part2_lightning"));
        return mr;
    }
}
