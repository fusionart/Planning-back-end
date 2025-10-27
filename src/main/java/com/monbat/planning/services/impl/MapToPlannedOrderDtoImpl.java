package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.planning.services.MapToPlannedOrderDto;
import com.monbat.planning.services.utils.HelperMethods;
import com.monbat.vdm.namespaces.opapiplannedorderssrv0001.PlannedOrder;
import com.monbat.vdm.namespaces.opapiplannedorderssrv0001.PlannedOrderCapacity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MapToPlannedOrderDtoImpl implements MapToPlannedOrderDto {
    @Override
    public List<PlannedOrderDto> getPlannedOrderList(List<PlannedOrder> plannedOrderList) {
        if (plannedOrderList == null) {
            return null;
        }

        List<PlannedOrderDto> plannedOrderDtoList = new ArrayList<>();
        for (PlannedOrder plannedOrder : plannedOrderList) {
            List<PlannedOrderCapacity> plannedOrderCapacityListLevel1 =
                    plannedOrder.getPlannedOrderCapacityOrFetch();

            Map<String, Object> plannedOrderCapacityListLevel2 = null;
            if (!plannedOrderCapacityListLevel1.isEmpty()){
                plannedOrderCapacityListLevel2 = (Map<String, Object>) plannedOrderCapacityListLevel1.getFirst();
            }

            PlannedOrderDto plannedOrderDto = new PlannedOrderDto();
            plannedOrderDto.setPlannedOrder(plannedOrder.getPlannedOrder());
            plannedOrderDto.setMaterial(plannedOrder.getMaterial());
            plannedOrderDto.setProductionPlan(plannedOrder.getProductionPlant());
            assert plannedOrder.getTotalOrderQuantity() != null;
            plannedOrderDto.setTotalQuantity(Double.valueOf(String.valueOf(plannedOrder.getTotalOrderQuantity())));
            plannedOrderDto.setSalesOrder(plannedOrder.getSalesOrder());

            plannedOrderDto.setProductionSupervisor(plannedOrder.getProductionSupervisor());

            if (plannedOrderCapacityListLevel2 != null){
                plannedOrderDto.setPlndOrderPlannedStartDate(HelperMethods.convertEpochDateToLocalDate(plannedOrderCapacityListLevel2.get(
                        "OpLtstSchedldProcgStrtDte").toString()));
                plannedOrderDto.setPlndOrderPlannedStartTime(HelperMethods.convertISO8601ToLocalTime(plannedOrderCapacityListLevel2.get(
                        "OpLtstSchedldProcgStrtTme").toString()));
                plannedOrderDto.setPlndOrderPlannedEndDate(HelperMethods.convertEpochDateToLocalDate(plannedOrderCapacityListLevel2.get(
                        "OpLtstSchedldTrdwnStrtDte").toString()));
                plannedOrderDto.setPlndOrderPlannedEndTime(HelperMethods.convertISO8601ToLocalTime(plannedOrderCapacityListLevel2.get(
                        "OpLtstSchedldTrdwnStrtTme").toString()));
                plannedOrderDto.setWorkCenter(plannedOrderCapacityListLevel2.get("WorkCenter").toString());
            }

            plannedOrderDto.setPlannedOrderCapacityIsDsptchd(Boolean.TRUE.equals(plannedOrder.getCapacityDispatched()));

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
