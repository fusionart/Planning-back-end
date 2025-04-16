package com.monbat.planning.models.dto.sap.production_order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionOrderWrapper {
    @JsonProperty("d")
    private ProductionOrderData d;
}
