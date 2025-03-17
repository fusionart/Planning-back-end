package com.monbat.planning.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatteryQuantityDto {
    private String batteryCode;
    private Integer quantity;
    private Integer productionPlant;
    private Integer storageLocation;
    private String batch;
}
