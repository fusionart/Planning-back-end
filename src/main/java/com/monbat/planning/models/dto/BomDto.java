package com.monbat.planning.models.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BomDto {
    private Integer plant;
    private String material;
    private Integer baseQuantity;
    private String component;
    private Double quantity;
    private String componentUom;
}
