package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.HarvestSeeds;

public class HarvestSeedsConverter implements DataConverter<HarvestSeeds> {

    @Override
    public HarvestSeeds convert(JsonNode row) {
        HarvestSeeds h = new HarvestSeeds();
        h.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        h.setPageName(row.path("_pageName").asText());
        h.setConsumedPrimalLifeforcePercentage(ItemConverter.parseIntSafe(row, "consumed_primal_lifeforce_percentage"));
        h.setConsumedVividLifeforcePercentage(ItemConverter.parseIntSafe(row, "consumed_vivid_lifeforce_percentage"));
        h.setConsumedWildLifeforcePercentage(ItemConverter.parseIntSafe(row, "consumed_wild_lifeforce_percentage"));
        h.setEffect(row.path("effect").asText());
        h.setGrantedCraftOptionIds(row.path("granted_craft_option_ids").asText());
        h.setGrowthCycles(ItemConverter.parseIntSafe(row, "growth_cycles"));
        h.setRequiredNearbySeedAmount(ItemConverter.parseIntSafe(row, "required_nearby_seed_amount"));
        h.setRequiredNearbySeedTier(ItemConverter.parseIntSafe(row, "required_nearby_seed_tier"));
        h.setTier(ItemConverter.parseIntSafe(row, "tier"));
        h.setType(row.path("type").asText());
        h.setTypeId(ItemConverter.parseIntSafe(row, "type_id"));
        return h;
    }
}
