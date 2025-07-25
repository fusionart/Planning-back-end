package com.monbat.planning.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaterialDto {
    private String material;
    private Integer plant;
    private String description;
    private String materialType;
    private String materialGroup;
    private String uom;
    private String externalMaterialGroup;
    private Integer leadTimeOffset;
    private Integer curringTime;
    private Integer netWeight;
}
