package com.monbat.planning.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductionSummaryDto {
    private LocalDate scheduleStartDate;
    private String workCenter;
    private String material;
    private Double targetQuantity;
    private Double deliveredQuantity;
    private String systemStatus;
    private String calendarWeek;
    private String productionVersion;
}
