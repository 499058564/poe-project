package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Version;

public class VersionConverter implements DataConverter<Version> {
    @Override
    public Version convert(JsonNode row) {
        Version v = new Version();
        v.setPageId(ItemConverter.parseIntSafe(row, "_pageID"));
        v.setPageName(ItemConverter.nullableText(row, "_pageName"));
        v.setAfter(ItemConverter.nullableText(row, "after"));
        v.setMajorPart(ItemConverter.parseIntSafe(row, "major_part"));
        v.setMinorPart(ItemConverter.parseIntSafe(row, "minor_part"));
        v.setPatchPart(ItemConverter.parseIntSafe(row, "patch_part"));
        v.setPrevious(ItemConverter.nullableText(row, "previous"));
        v.setReleaseDate(ItemConverter.nullableText(row, "release_date"));
        v.setRevisionPart(ItemConverter.nullableText(row, "revision_part"));
        v.setVersion(ItemConverter.nullableText(row, "version"));
        return v;
    }
}
