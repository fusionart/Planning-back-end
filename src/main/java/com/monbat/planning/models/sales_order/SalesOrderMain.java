package com.monbat.planning.models.sales_order;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SalesOrderMain implements Serializable {
    private String material;
    private Double requestedQuantity;
    private Double toProduce;
    private Double totalAvailableQuantity;
    private Double cumulativeQuantity;
    private Double availableNotCharged;
    private Double availableCharged;
    private Double finalBattery;
    private String requestedQuantityUnit;
    private String plant;
    private final Map<String, SalesOrderMainItem> dynamicSoItems;

    public SalesOrderMain(String material, double requestedQuantity, String plant,
                          String requestedQuantityUnit, double availableNotCharged, double availableCharged,
                          double finalBattery, double cumulativeQuantity) {
        this.material = material;
        this.requestedQuantity = requestedQuantity;
        this.requestedQuantityUnit = requestedQuantityUnit;
        this.plant = plant;
        this.availableCharged = availableCharged;
        this.availableNotCharged = availableNotCharged;
        this.finalBattery = finalBattery;
        this.cumulativeQuantity = cumulativeQuantity;
        this.dynamicSoItems = new HashMap<>();
    }

    public void addDynamicSoValue(String columnName, SalesOrderMainItem value) {
        dynamicSoItems.put(columnName, value);
    }

    public SalesOrderMainItem getDynamicSoValue(String columnName) {
        return dynamicSoItems.get(columnName);
    }
}
