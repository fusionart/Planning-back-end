package com.monbat.planning.services;

import com.monbat.planning.models.dto.sap.production_order.ProductionOrderDto;
import com.monbat.vdm.namespaces.opapiproductionorder2srv0001.ProductionOrderComponents;

import java.util.List;

public interface MapToProductionOrderDto {
    List<ProductionOrderDto> productionOrderList(List<ProductionOrderComponents> productionOrderComponentsList);
}
