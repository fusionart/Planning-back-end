package com.monbat.planning.models.sales_order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ToItem {
    private String material;
    private Double requestedQuantity;
    private String requestedQuantityUnit;
    private String SDProcessStatus;
}
