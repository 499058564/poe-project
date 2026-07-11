package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 数据转换器接口，将 Wiki Cargo 返回的单行 JSON（{@code cargoquery[].title}）
 * 转换为对应的数据模型对象。
 *
 * @param <T> 目标模型类型
 */
@FunctionalInterface
public interface DataConverter<T> {

    /**
     * 将 Wiki Cargo 单行 JSON 转换为模型对象。
     *
     * @param wikiRow cargoquery[i].title 节点，字段名与 Cargo 表列名一致
     * @return 转换后的模型对象，字段缺失时使用默认值（0 / 0.0 / null）
     */
    T convert(JsonNode wikiRow);
}
