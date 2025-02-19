package com.monbat.planning.models.other;

import com.monbat.planning.models.entities.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkCenterByBaseQuantity {
    private String reqDlvDate;
    private String workCenter;
    private Integer baseQuantity;
    private Integer quantity;
    private ActivityType machineTime;
    private ActivityType laborTime;
}
