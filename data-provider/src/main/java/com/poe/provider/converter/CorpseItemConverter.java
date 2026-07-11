package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.CorpseItem;

public class CorpseItemConverter implements DataConverter<CorpseItem> {
    @Override
    public CorpseItem convert(JsonNode row) {
        CorpseItem v = new CorpseItem();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setMonsterAbilities(ItemConverter.nullableText(row, "monster_abilities"));
        v.setMonsterCategory(ItemConverter.nullableText(row, "monster_category"));
        v.setMonsterCategoryHtml(ItemConverter.nullableText(row, "monster_category_html"));
        v.setTier(ItemConverter.parseIntSafe(row, "tier"));
        return v;
    }
}
