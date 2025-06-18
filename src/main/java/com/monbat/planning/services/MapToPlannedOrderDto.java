package com.monbat.planning.services;

import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.vdm.namespaces.opapiplannedorderssrv0001.PlannedOrder;

import java.util.List;

public interface MapToPlannedOrderDto {
    List<PlannedOrderDto> getPlannedOrderList(List<PlannedOrder> plannedOrderList);
}
