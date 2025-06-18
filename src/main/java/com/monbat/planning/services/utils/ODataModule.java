package com.monbat.planning.services.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ODataModule extends SimpleModule {
    public ODataModule() {
        // Register deserializer for List<ItemPartner>
        addDeserializer(List.class, new ODataListDeserializer());
    }
}

class ODataListDeserializer extends JsonDeserializer<List<?>> {
    @Override
    public List<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // Case 1: OData deferred structure (__deferred)
        if (node.has("__deferred")) {
            return new ArrayList<>(); // Return empty list (or fetch later)
        }
        // Case 2: OData expanded results (results array)
        else if (node.has("results")) {
            JsonNode results = node.get("results");
            if (results.isArray()) {
                List<Object> list = new ArrayList<>();
                for (JsonNode item : results) {
                    // Deserialize each item dynamically
                    Object value = p.getCodec().treeToValue(item, Object.class);
                    list.add(value);
                }
                return list;
            }
        }
        // Case 3: Direct array (fallback)
        else if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                Object value = p.getCodec().treeToValue(item, Object.class);
                list.add(value);
            }
            return list;
        }

        // Default: Return empty list
        return new ArrayList<>();
    }
}
