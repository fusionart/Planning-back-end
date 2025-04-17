package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.sap.sales_order.SalesOrderDto;
import com.monbat.planning.models.dto.sap.sales_order.ToItem;
import com.monbat.planning.services.MapToSalesOrderDto;
import com.monbat.vdm.namespaces.apisalesordersrv.SalesOrderHeader;
import com.monbat.vdm.namespaces.apisalesordersrv.SalesOrderItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MapToSalesOrderDtoImpl implements MapToSalesOrderDto {
    @Override
    public List<SalesOrderDto> salesOrderList(List<SalesOrderHeader> salesOrderComponentsList) {
        List<SalesOrderDto> salesOrders = new ArrayList<>();

        for (SalesOrderHeader salesOrderHeader : salesOrderComponentsList){
            SalesOrderDto salesOrderDto = new SalesOrderDto();
            salesOrderDto.setSalesOrderNumber(salesOrderHeader.getSalesOrder());
            salesOrderDto.setSoldToParty(salesOrderHeader.getSoldtoParty());
            salesOrderDto.setRequestedDeliveryDate(salesOrderHeader.getRequestedDeliveryDate());
            salesOrderDto.setRequestedDeliveryWeek(salesOrderHeader.getRequestedDeliveryDate().get(WeekFields.of(Locale.getDefault()).weekOfYear()) + "/" + salesOrderHeader.getRequestedDeliveryDate().getYear());
            List<ToItem> toItemList = getToItems(salesOrderHeader);
            salesOrderDto.setToItem(toItemList);
            salesOrders.add(salesOrderDto);
        }
        return salesOrders;
    }

    @NotNull
    private static List<ToItem> getToItems(SalesOrderHeader salesOrderHeader) {
        List<ToItem> toItemList = new ArrayList<>();
        for (SalesOrderItem salesOrderItem : salesOrderHeader.getItemOrFetch()){
            ToItem toItem = new ToItem();
            toItem.setMaterial(salesOrderItem.getMaterial());
            toItem.setRequestedQuantity(salesOrderItem.getRequestedQuantity().doubleValue());
            toItem.setRequestedQuantityUnit(salesOrderItem.getRequestedQtyUnit());
            toItem.setSDProcessStatus(salesOrderItem.getOverallStatus());
            toItemList.add(toItem);
        }
        return toItemList;
    }
}
