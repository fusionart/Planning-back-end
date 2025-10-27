package com.monbat.planning.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class PlannedOrderDto {
    private String plannedOrder;
    private String material;
    private String productionPlan;
    private Double totalQuantity;
    private String salesOrder;
    private String productionSupervisor;
    private LocalDate plndOrderPlannedStartDate;
    private LocalTime plndOrderPlannedStartTime;
    private LocalDate plndOrderPlannedEndDate;
    private LocalTime plndOrderPlannedEndTime;
    private boolean plannedOrderCapacityIsDsptchd;
    private String workCenter;
    private String etag;
}
