package com.monbat.planning.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadinessDto {
    private Integer salesDocument;
    private String soldToParty;
    private String customerName;
    private Date dateOfReadiness;
    private String weekOfReadiness;
    private String reqDlvWeek;
    private String material;
    private Integer orderQuantity;
    private Integer productionPlant;
    private String batteryType;
}
