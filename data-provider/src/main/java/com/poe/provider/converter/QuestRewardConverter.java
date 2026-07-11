package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.QuestReward;

public class QuestRewardConverter implements DataConverter<QuestReward> {
    @Override
    public QuestReward convert(JsonNode row) {
        QuestReward v = new QuestReward();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setAct(ItemConverter.parseIntSafe(row, "act"));
        v.setClassIds(ItemConverter.nullableText(row, "class_ids"));
        v.setClasses(ItemConverter.nullableText(row, "classes"));
        v.setItemLevel(ItemConverter.parseIntSafe(row, "item_level"));
        v.setNotes(ItemConverter.nullableText(row, "notes"));
        v.setQuest(ItemConverter.nullableText(row, "quest"));
        v.setQuestId(ItemConverter.nullableText(row, "quest_id"));
        v.setRarity(ItemConverter.nullableText(row, "rarity"));
        v.setSockets(ItemConverter.parseIntSafe(row, "sockets"));
        return v;
    }
}
