package com.monbat.planning.models.production_order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monbat.vdm.namespaces.opapiproductionorder2srv0001.ProductionOrderComponents;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductionOrderData {
    @JsonProperty("results")
    private List<ProductionOrderComponents> results;
}
