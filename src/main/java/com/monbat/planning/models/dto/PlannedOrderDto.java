package com.monbat.planning.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlannedOrderDto {
    private String plannedOrder;
    private String material;
    private String productionPlan;
    private Double totalQuantity;
    private String salesOrder;
}
