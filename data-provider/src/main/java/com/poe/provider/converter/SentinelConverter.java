package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Sentinel;

public class SentinelConverter implements DataConverter<Sentinel> {
    @Override
    public Sentinel convert(JsonNode row) {
        Sentinel v = new Sentinel();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setCharge(ItemConverter.parseIntSafe(row, "charge"));
        v.setChargeHtml(ItemConverter.nullableText(row, "charge_html"));
        v.setChargeRangeAverage(ItemConverter.parseIntSafe(row, "charge_range_average"));
        v.setChargeRangeColour(ItemConverter.nullableText(row, "charge_range_colour"));
        v.setChargeRangeMaximum(ItemConverter.parseIntSafe(row, "charge_range_maximum"));
        v.setChargeRangeMinimum(ItemConverter.parseIntSafe(row, "charge_range_minimum"));
        v.setChargeRangeText(ItemConverter.nullableText(row, "charge_range_text"));
        v.setDuration(ItemConverter.parseIntSafe(row, "duration"));
        v.setDurationHtml(ItemConverter.nullableText(row, "duration_html"));
        v.setDurationRangeAverage(ItemConverter.parseIntSafe(row, "duration_range_average"));
        v.setDurationRangeColour(ItemConverter.nullableText(row, "duration_range_colour"));
        v.setDurationRangeMaximum(ItemConverter.parseIntSafe(row, "duration_range_maximum"));
        v.setDurationRangeMinimum(ItemConverter.parseIntSafe(row, "duration_range_minimum"));
        v.setDurationRangeText(ItemConverter.nullableText(row, "duration_range_text"));
        v.setEmpowerment(ItemConverter.parseIntSafe(row, "empowerment"));
        v.setEmpowermentHtml(ItemConverter.nullableText(row, "empowerment_html"));
        v.setEmpowermentRangeAverage(ItemConverter.parseIntSafe(row, "empowerment_range_average"));
        v.setEmpowermentRangeColour(ItemConverter.nullableText(row, "empowerment_range_colour"));
        v.setEmpowermentRangeMaximum(ItemConverter.parseIntSafe(row, "empowerment_range_maximum"));
        v.setEmpowermentRangeMinimum(ItemConverter.parseIntSafe(row, "empowerment_range_minimum"));
        v.setEmpowermentRangeText(ItemConverter.nullableText(row, "empowerment_range_text"));
        v.setEmpowers(ItemConverter.parseIntSafe(row, "empowers"));
        v.setEmpowersHtml(ItemConverter.nullableText(row, "empowers_html"));
        v.setEmpowersRangeAverage(ItemConverter.parseIntSafe(row, "empowers_range_average"));
        v.setEmpowersRangeColour(ItemConverter.nullableText(row, "empowers_range_colour"));
        v.setEmpowersRangeMaximum(ItemConverter.parseIntSafe(row, "empowers_range_maximum"));
        v.setEmpowersRangeMinimum(ItemConverter.parseIntSafe(row, "empowers_range_minimum"));
        v.setEmpowersRangeText(ItemConverter.nullableText(row, "empowers_range_text"));
        v.setMonster(ItemConverter.nullableText(row, "monster"));
        v.setMonsterLevel(ItemConverter.parseIntSafe(row, "monster_level"));
        return v;
    }
}
