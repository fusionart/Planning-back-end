package com.monbat.planning.models.other;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

@Getter
public class QuantityByWorkCenter {
    private final Map<String, Integer> quantityByWorkCenter = new TreeMap<>();

    public Integer get(String key) {
        return quantityByWorkCenter.get(key);
    }

    public boolean contains(String key){
        return !quantityByWorkCenter.containsKey(key);
    }

    public void put(String key, Integer value) {
        quantityByWorkCenter.put(key, value);
    }
}
