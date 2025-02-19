package com.monbat.planning.models.other;

import java.util.Map;
import java.util.TreeMap;

public class AllWorkCentersByWeek {
    private final Map<String, AllWorkCentersByWeekByWarehouse> allWorkCentersByWeek = new TreeMap<>();

    public AllWorkCentersByWeekByWarehouse get(String key) {
        return allWorkCentersByWeek.get(key);
    }

    public void put(String key, AllWorkCentersByWeekByWarehouse value) {
        allWorkCentersByWeek.put(key, value);
    }

    public Map<String, AllWorkCentersByWeekByWarehouse> getMap(){
        return allWorkCentersByWeek;
    }
}
