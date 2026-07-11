package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Prophecy;

public class ProphecyConverter implements DataConverter<Prophecy> {
    @Override
    public Prophecy convert(JsonNode row) {
        Prophecy v = new Prophecy();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setObjective(ItemConverter.nullableText(row, "objective"));
        v.setPredictionText(ItemConverter.nullableText(row, "prediction_text"));
        v.setProphecyId(ItemConverter.nullableText(row, "prophecy_id"));
        v.setReward(ItemConverter.nullableText(row, "reward"));
        v.setSealCost(ItemConverter.parseIntSafe(row, "seal_cost"));
        return v;
    }
}
