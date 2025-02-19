package com.monbat.planning.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductionVersionDto {
    private String material;
    private Integer plant;
    private Integer productionVersionNumber;
    private String routingGroup;
    private Integer routingGroupCounter;
    private String description;
}
