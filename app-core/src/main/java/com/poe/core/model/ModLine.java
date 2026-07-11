package com.poe.core.model;

/**
 * 单条词缀行，用于展示物品的显式词缀或基底词缀。
 * <p>
 * 示例：{@code "+20 to maximum Life"} 或 {@code "Adds 10 to 20 Fire Damage"}。
 */
public class ModLine {
    /** 词缀描述文本 */
    private String text;

    /** 词缀值范围（如有数值，如 "20" 或 "10-20"） */
    private String value;

    /** 词缀类型：prefix/suffix/implicit/enchant */
    private String type;

    public ModLine() {}

    public ModLine(String text, String value, String type) {
        this.text = text;
        this.value = value;
        this.type = type;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
