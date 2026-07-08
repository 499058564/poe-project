package com.poe.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void toJsonShouldSerializeObject() {
        TestItem item = new TestItem("Chaos Orb", 1);
        String json = JsonUtils.toJson(item);
        assertTrue(json.contains("\"name\":\"Chaos Orb\""));
        assertTrue(json.contains("\"value\":1"));
    }

    @Test
    void fromJsonShouldDeserializeObject() {
        String json = "{\"name\":\"Divine Orb\",\"value\":200}";
        TestItem item = JsonUtils.fromJson(json, TestItem.class);
        assertEquals("Divine Orb", item.name);
        assertEquals(200, item.value);
    }

    @Test
    void fromJsonListShouldDeserializeList() {
        String json = "[{\"name\":\"a\",\"value\":1},{\"name\":\"b\",\"value\":2}]";
        List<TestItem> items = JsonUtils.fromJsonList(json, TestItem.class);
        assertEquals(2, items.size());
        assertEquals("a", items.get(0).name);
        assertEquals("b", items.get(1).name);
    }

    @Test
    void toJsonAndFromJsonRoundTrip() {
        TestItem original = new TestItem("Mageblood", 350);
        String json = JsonUtils.toJson(original);
        TestItem restored = JsonUtils.fromJson(json, TestItem.class);
        assertEquals(original.name, restored.name);
        assertEquals(original.value, restored.value);
    }

    @Test
    void fromJsonShouldThrowOnInvalidJson() {
        assertThrows(RuntimeException.class, () ->
            JsonUtils.fromJson("not valid json", TestItem.class));
    }

    // Simple POJO for testing
    public static class TestItem {
        public String name;
        public int value;

        public TestItem() {}

        public TestItem(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
