package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.planning.services.MapToPlannedOrderDto;
import com.monbat.vdm.namespaces.opapiplannedorderssrv0001.PlannedOrder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            plannedOrderDto.setTotalQuantity(Double.valueOf(String.valueOf(plannedOrder.getTotalOrderQuantity())));
            plannedOrderDto.setSalesOrder(plannedOrder.getSalesOrder());

            String etag = extractEtagFromPlannedOrder(plannedOrder);
            plannedOrderDto.setEtag(etag);

            plannedOrderDtoList.add(plannedOrderDto);
        }
        return plannedOrderDtoList;
    }

    private String extractEtagFromPlannedOrder(PlannedOrder plannedOrder) {
        try {
            // Access the custom fields map
            Map<String, Object> customFields = plannedOrder.getCustomFields();
            // Get the __metadata map
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) customFields.get("__metadata");
            if (metadata != null) {
                // Extract etag value
                return (String) metadata.get("etag");
            }
        } catch (Exception e) {
            // Handle potential casting or null pointer exceptions
            System.err.println("Error extracting etag: " + e.getMessage());
        }
        return null; // or empty string based on your requirement
    }
}
