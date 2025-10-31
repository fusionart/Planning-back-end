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
            // Get the capacities as entity objects (not Maps)
            List<PlannedOrderCapacity> plannedOrderCapacities = plannedOrder.getPlannedOrderCapacityOrFetch();

            PlannedOrderDto plannedOrderDto = mapBasicPlannedOrderFields(plannedOrder);

            Map<String, Object> capacityData = extractCapacityDataFromCustomFields(plannedOrderCapacities);
            if (capacityData != null) {
                mapCapacityFields(plannedOrderDto, capacityData);
            } else {
                setDefaultCapacityFields(plannedOrderDto);
            }

            plannedOrderDtoList.add(plannedOrderDto);
        }
        return plannedOrderDtoList;
    }

    private PlannedOrderDto mapBasicPlannedOrderFields(PlannedOrder plannedOrder) {
        PlannedOrderDto dto = new PlannedOrderDto();
        dto.setPlannedOrder(plannedOrder.getPlannedOrder());
        dto.setMaterial(plannedOrder.getMaterial());
        dto.setProductionPlan(plannedOrder.getProductionPlant());
        dto.setTotalQuantity(Double.valueOf(String.valueOf(plannedOrder.getTotalOrderQuantity())));
        dto.setSalesOrder(plannedOrder.getSalesOrder());
        dto.setProductionSupervisor(plannedOrder.getProductionSupervisor());
        dto.setDescription(plannedOrder.getMaterialDescription());
        dto.setPlannedOrderCapacityIsDsptchd(Boolean.TRUE.equals(plannedOrder.getCapacityDispatched()));

        String etag = extractEtagFromPlannedOrder(plannedOrder);
        dto.setEtag(etag);
        return dto;
    }

    private Map<String, Object> extractCapacityDataFromCustomFields(List<PlannedOrderCapacity> plannedOrderCapacities) {
        if (plannedOrderCapacities.isEmpty()) {
            return null;
        }

        PlannedOrderCapacity firstCapacity = plannedOrderCapacities.get(0);

        // Get the customFields from the PlannedOrderCapacity object
        Map<String, Object> customFields = firstCapacity.getCustomFields();
        if (customFields == null || customFields.isEmpty()) {
            return null;
        }

        // Extract the results from customFields
        Object resultsObj = customFields.get("results");
        if (!(resultsObj instanceof List) || ((List<?>) resultsObj).isEmpty()) {
            return null;
        }

        Object firstResult = ((List<?>) resultsObj).get(0);
        return (firstResult instanceof Map) ? (Map<String, Object>) firstResult : null;
    }

    private void mapCapacityFields(PlannedOrderDto dto, Map<String, Object> capacityData) {
        dto.setWorkCenter(getStringValue(capacityData, "WorkCenter"));

         dto.setPlndOrderPlannedStartDate(
             HelperMethods.convertEpochDateToLocalDate(getStringValue(capacityData, "OpLtstSchedldProcgStrtDte")));
         dto.setPlndOrderPlannedStartTime(
             HelperMethods.convertISO8601ToLocalTime(getStringValue(capacityData, "OpLtstSchedldProcgStrtTme")));
         dto.setPlndOrderPlannedEndDate(
             HelperMethods.convertEpochDateToLocalDate(getStringValue(capacityData, "OpLtstSchedldTrdwnStrtDte")));
         dto.setPlndOrderPlannedEndTime(
             HelperMethods.convertISO8601ToLocalTime(getStringValue(capacityData, "OpLtstSchedldTrdwnStrtTme")));
    }

    private void setDefaultCapacityFields(PlannedOrderDto dto) {
        dto.setPlndOrderPlannedStartDate(null);
        dto.setPlndOrderPlannedStartTime(null);
        dto.setPlndOrderPlannedEndDate(null);
        dto.setPlndOrderPlannedEndTime(null);
        dto.setWorkCenter(null);
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

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
