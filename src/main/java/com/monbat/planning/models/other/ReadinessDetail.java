package com.monbat.planning.models.other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadinessDetail {
    private Integer salesDocument;
    private String soldToParty;
    private String customerName;
    private String reqDlvWeek;
    private String material;
    private Integer orderQuantity;
    private Integer productionPlant;
    private String batteryType;
    private String workCenter;
}
