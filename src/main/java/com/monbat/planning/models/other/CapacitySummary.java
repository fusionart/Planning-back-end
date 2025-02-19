package com.monbat.planning.models.other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CapacitySummary {
    private String productionWorkshop;
    private String workCenter;
    private String description;
    private Integer quantity;
    private String calculatedShifts;
    private String personnel;
    private Integer norm;
    private Double produced;
    private Integer workingDays;
    private Integer shifts;
    private String enterShifts;
}
