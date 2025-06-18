package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.planning.services.MapToPlannedOrderDto;
import com.monbat.vdm.namespaces.opapiplannedorderssrv0001.PlannedOrder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MapToPlannedOrderDtoImpl implements MapToPlannedOrderDto {
    @Override
    public List<PlannedOrderDto> getPlannedOrderList(List<PlannedOrder> plannedOrderList) {
        List<PlannedOrderDto> plannedOrderDtoList = new ArrayList<>();
        for (PlannedOrder plannedOrder : plannedOrderList) {
            PlannedOrderDto plannedOrderDto = new PlannedOrderDto();
            plannedOrderDto.setPlannedOrder(plannedOrder.getPlannedOrder());
            plannedOrderDto.setMaterial(plannedOrder.getMaterial());
            plannedOrderDto.setProductionPlan(plannedOrder.getProductionPlant());
            assert plannedOrder.getTotalOrderQuantity() != null;
            plannedOrderDto.setTotalQuantity(plannedOrder.getTotalOrderQuantity().doubleValue());
            plannedOrderDto.setSalesOrder(plannedOrder.getSalesOrder());
            plannedOrderDtoList.add(plannedOrderDto);
        }
        return plannedOrderDtoList;
    }
}
