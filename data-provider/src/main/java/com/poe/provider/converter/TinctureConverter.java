package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Tincture;

public class TinctureConverter implements DataConverter<Tincture> {
    @Override
    public Tincture convert(JsonNode row) {
        Tincture v = new Tincture();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setCooldown(ItemConverter.parseDoubleSafe(row, "cooldown"));
        v.setCooldownHtml(ItemConverter.nullableText(row, "cooldown_html"));
        v.setCooldownRangeAverage(ItemConverter.parseDoubleSafe(row, "cooldown_range_average"));
        v.setCooldownRangeColour(ItemConverter.nullableText(row, "cooldown_range_colour"));
        v.setCooldownRangeMaximum(ItemConverter.parseDoubleSafe(row, "cooldown_range_maximum"));
        v.setCooldownRangeMinimum(ItemConverter.parseDoubleSafe(row, "cooldown_range_minimum"));
        v.setCooldownRangeText(ItemConverter.nullableText(row, "cooldown_range_text"));
        v.setDebuffInterval(ItemConverter.parseDoubleSafe(row, "debuff_interval"));
        v.setDebuffIntervalHtml(ItemConverter.nullableText(row, "debuff_interval_html"));
        v.setDebuffIntervalRangeAverage(ItemConverter.parseDoubleSafe(row, "debuff_interval_range_average"));
        v.setDebuffIntervalRangeColour(ItemConverter.nullableText(row, "debuff_interval_range_colour"));
        v.setDebuffIntervalRangeMaximum(ItemConverter.parseDoubleSafe(row, "debuff_interval_range_maximum"));
        v.setDebuffIntervalRangeMinimum(ItemConverter.parseDoubleSafe(row, "debuff_interval_range_minimum"));
        v.setDebuffIntervalRangeText(ItemConverter.nullableText(row, "debuff_interval_range_text"));
        return v;
    }
}
