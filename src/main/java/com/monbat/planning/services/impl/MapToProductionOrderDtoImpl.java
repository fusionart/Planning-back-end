package com.monbat.planning.services.impl;

import com.monbat.planning.models.production_order.ProductionOrderDto;
import com.monbat.planning.services.MapToProductionOrderDto;
import com.monbat.planning.services.utils.HelperMethods;
import com.monbat.vdm.namespaces.opapiproductionorder2srv0001.ProductionOrderComponents;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MapToProductionOrderDtoImpl implements MapToProductionOrderDto {

    @Override
    public List<ProductionOrderDto> productionOrderList(List<ProductionOrderComponents> productionOrderComponentsList) {
        List<ProductionOrderDto> productionOrderDtoList = new ArrayList<>();

        for (ProductionOrderComponents productionOrderComponent : productionOrderComponentsList){
            Map<String, Object> customFields = productionOrderComponent.getCustomFields();

            ProductionOrderDto productionOrderDto = new ProductionOrderDto();
            productionOrderDto.setMaterial(productionOrderComponent.getMaterial());
            //productionOrderDto.setMaterialDescription(this.materialService.getMaterialByCode
            // (productionOrderComponent.getMaterial()).getDescription());
            productionOrderDto.setProductionOrder(productionOrderComponent.getProductionOrder());
            productionOrderDto.setProductionPlant(productionOrderComponent.getProductionPlant());

            productionOrderDto.setOrderIsReleased(!customFields.get("OrderIsReleased").toString().isEmpty());
            productionOrderDto.setOrderIsScheduled(!customFields.get("OrderIsScheduled").toString().isEmpty());
            productionOrderDto.setProductionSupervisor(customFields.get("ProductionSupervisor").toString());
            productionOrderDto.setProductionVersion(customFields.get("ProductionVersion").toString());
            productionOrderDto.setMfgOrderScheduledStartDate(HelperMethods.convertEpochDateToLocalDate(customFields.get(
                    "MfgOrderScheduledStartDate").toString()));
            productionOrderDto.setMfgOrderScheduledStartTime(HelperMethods.convertISO8601ToLocalTime(customFields.get(
                    "MfgOrderScheduledStartTime").toString()));
            productionOrderDto.setMfgOrderScheduledEndDate(HelperMethods.convertEpochDateToLocalDate(customFields.get(
                    "MfgOrderScheduledEndDate").toString()));
            productionOrderDto.setMfgOrderScheduledEndTime(HelperMethods.convertISO8601ToLocalTime(customFields.get(
                    "MfgOrderScheduledEndTime").toString()));
            productionOrderDto.setProductionUnit(customFields.get("ProductionUnit").toString());
            productionOrderDto.setTotalQuantity(Double.parseDouble(customFields.get("TotalQuantity").toString()));
            productionOrderDto.setMfgOrderConfirmedYieldQty(Double.parseDouble(customFields.get("MfgOrderConfirmedYieldQty").toString()));
            productionOrderDto.setSalesOrder(customFields.get("SalesOrder").toString());

            LinkedHashMap<String, Object> customFieldsProdOperations =
                    (LinkedHashMap<String, Object>) productionOrderComponent.getCustomFields().get(
                            "to_ProductionOrderOperation");

            ArrayList results = (ArrayList) customFieldsProdOperations.get("results");
            for (Object item : results.stream().toList()){
                Map<String, Object> subItem = (Map<String, Object>) item;
                if (!subItem.get("ManufacturingOrderOperation").equals("0020")){
                    productionOrderDto.setWorkCenter(subItem.get("WorkCenter").toString());
                    productionOrderDto.setWorkCenterDescription(subItem.get("MfgOrderOperationText").toString());
                };
            }

            productionOrderDtoList.add(productionOrderDto);
        }


        return productionOrderDtoList;
    }
}
