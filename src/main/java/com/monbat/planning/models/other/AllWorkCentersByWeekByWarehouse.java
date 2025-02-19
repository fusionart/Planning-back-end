package com.monbat.planning.models.other;

import java.util.LinkedHashMap;
import java.util.Map;

public class AllWorkCentersByWeekByWarehouse {
    private final Map<String, QuantityByWorkCenter> allWorkCentersByWeekByWarehouse = new LinkedHashMap<>();

    public QuantityByWorkCenter get(String key) {
        return allWorkCentersByWeekByWarehouse.get(key);
    }

    public void put(String key, QuantityByWorkCenter value) {
        allWorkCentersByWeekByWarehouse.put(key, value);
    }

    public void getMapKey() {
        //allWorkCentersByWeekByWarehouse
    }

    public Map<String, QuantityByWorkCenter> getMap(){
        return allWorkCentersByWeekByWarehouse;
    }
}
