package com.monbat.planning.models.other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductionExecutionSummary {
    private String workCenter;
    private String date;
    private Integer shift;
    private Double orderQuantity;
    private Double deliveredQuantity;
    private Double setupQuantity;
    private Integer norm;
}
