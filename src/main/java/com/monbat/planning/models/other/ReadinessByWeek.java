package com.monbat.planning.models.other;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadinessByWeek implements Serializable {
    private final Map<String, List<ReadinessDetailWithDate>> readinessByWeek = new HashMap<>();

    public List<ReadinessDetailWithDate> get(String key) {
        return readinessByWeek.get(key);
    }

    public void put(String key, List<ReadinessDetailWithDate> value) {
        readinessByWeek.put(key, value);
    }

    public Map<String, List<ReadinessDetailWithDate>> getMap(){
        return readinessByWeek;
    }
}
