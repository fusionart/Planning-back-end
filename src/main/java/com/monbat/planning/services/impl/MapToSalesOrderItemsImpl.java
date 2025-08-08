package com.monbat.planning.services.impl;

import com.monbat.planning.controllers.MaterialController;
import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.planning.models.entities.Material;
import com.monbat.planning.models.production_order.ProductionOrderDto;
import com.monbat.planning.models.sales_order.SalesOrderByDate;
import com.monbat.planning.models.sales_order.SalesOrderMain;
import com.monbat.planning.models.sales_order.SalesOrderMainItem;
import com.monbat.planning.services.MaterialStockService;
import com.monbat.vdm.namespaces.opapisalesordersrv0001.SalesOrderHeader;
import com.monbat.vdm.namespaces.opapisalesordersrv0001.SalesOrderItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("unchecked")
@Service
public class MapToSalesOrderItemsImpl {
    @Autowired
    private MaterialController materialController;
    @Autowired
    private ProductionOrderServiceImpl productionOrderService;
    @Autowired
    private PlannedOrderServiceImpl plannedOrderService;
    @Autowired
    private MaterialStockService materialStockService;

    public List<SalesOrderByDate> generateSalesOrderMainData(List<SalesOrderHeader> salesOrderHeaders, String username,
                                                           String password, LocalDateTime reqDelDateBegin, LocalDateTime reqDelDateEnd) {

        List<SalesOrderByDate> salesOrderByDateList = new ArrayList<>();
        List<SalesOrderMain> salesOrderMainList = new ArrayList<>();
        List<Material> materialList = (List<Material>) this.materialController.getMaterials();

        List<PlannedOrderDto> plannedOrderList = this.plannedOrderService.getPlannedOrders(username, password,
                reqDelDateBegin, reqDelDateEnd);
        List<ProductionOrderDto> productionOrderList = this.productionOrderService.getProductionOrders(username,
                password, reqDelDateBegin, reqDelDateBegin);

        for (SalesOrderHeader salesOrderHeader : salesOrderHeaders) {
            String targetWeek =
                    salesOrderHeader.getRequestedDeliveryDate().get(WeekFields.of(Locale.getDefault()).weekOfYear()) + "/" + salesOrderHeader.getRequestedDeliveryDate().getYear();
            Optional<SalesOrderByDate> foundOrder = salesOrderByDateList.stream()
                    .filter(order -> targetWeek.equals(order.getReqDlvWeek()))
                    .findFirst();

            SalesOrderByDate salesOrderByDate;
            if (foundOrder.isPresent()) {
                salesOrderByDate = foundOrder.get();
                salesOrderMainList = salesOrderByDate.getSalesOrderMainList();
            } else {
                salesOrderByDate = new SalesOrderByDate();
                salesOrderByDate.setReqDlvWeek(targetWeek);
                salesOrderByDateList.add(salesOrderByDate);
                salesOrderMainList = new ArrayList<>();
            }

            for (SalesOrderItem salesOrderItem : salesOrderHeader.getItemOrFetch()) {
                boolean exists = salesOrderMainList.stream()
                        .anyMatch(item -> {
                            assert salesOrderItem.getMaterial() != null;
                            return salesOrderItem.getMaterial().equals(item.getMaterial());
                        });

                String plannedOrder = plannedOrderList.stream()
                        .filter(material -> material.getMaterial().equals(salesOrderItem.getMaterial()))
                        .filter(so -> so.getSalesOrder().equals(salesOrderHeader.getSalesOrder()))
                        .findFirst()
                        .map(PlannedOrderDto::getPlannedOrder)
                        .orElse("");

                String productionOrder = productionOrderList.stream()
                        .filter(material -> material.getMaterial().equals(salesOrderItem.getMaterial()))
                        .filter(so -> so.getSalesOrder().equals(salesOrderHeader.getSalesOrder()))
                        .findFirst()
                        .map(ProductionOrderDto::getProductionOrder)
                        .orElse("");

                if (!exists) {
                    String plantName = getPlantName(salesOrderItem, materialList);
                    assert salesOrderItem.getMaterial() != null;
                    double notChargedQuantity =
                            this.materialStockService.getMaterialStock(username, password,
                                    "20" + StringUtils.substring(salesOrderItem.getMaterial(), 2,
                                            salesOrderItem.getMaterial().length() - 1) + "2");
                    double chargedQuantity =
                            this.materialStockService.getMaterialStock(username, password,
                                    "11" + StringUtils.right(salesOrderItem.getMaterial(),
                                            salesOrderItem.getMaterial().length() - 2));

                    assert salesOrderItem.getRequestedQuantity() != null;
                    SalesOrderMain salesOrderMain = new SalesOrderMain(salesOrderItem.getMaterial(),
                            salesOrderItem.getRequestedQuantity().doubleValue(), plantName,
                            salesOrderItem.getRequestedQtyUnit(),
                            notChargedQuantity,
                            chargedQuantity);

                    salesOrderMain.addDynamicSoValue(salesOrderHeader.getSalesOrder(),
                            new SalesOrderMainItem(salesOrderItem.getRequestedQuantity().doubleValue(), plannedOrder,
                                    productionOrder, salesOrderHeader.getCompleteDelivery(), salesOrderHeader.getSoldtoParty()));

                    salesOrderMainList.add(salesOrderMain);

                } else {
                    Optional<SalesOrderMain> foundItem = salesOrderMainList.stream()
                            .filter(item -> salesOrderItem.getMaterial().equals(item.getMaterial()))
                            .findFirst();

                    foundItem.ifPresent(item -> {
                        item.setRequestedQuantity(item.getRequestedQuantity() + salesOrderItem.getRequestedQuantity().doubleValue());
                        item.addDynamicSoValue(salesOrderHeader.getSalesOrder(),
                                new SalesOrderMainItem(salesOrderItem.getRequestedQuantity().doubleValue(),
                                        plannedOrder, productionOrder, salesOrderHeader.getCompleteDelivery(), salesOrderHeader.getSoldtoParty()));
                    });
                }
            }
            salesOrderByDate.setSalesOrderMainList(salesOrderMainList);
        }
        return salesOrderByDateList;
    }

    private static String getPlantName(SalesOrderItem salesOrderItem, List<Material> materialList) {
        int plant = materialList.stream()
                .filter(material -> {
                    assert salesOrderItem.getMaterial() != null;
                    return salesOrderItem.getMaterial().equals(material.getMaterial());
                })
                .findFirst()
                .map(Material::getPlant)
                .orElse(0);

        String plantName = switch (plant) {
            case 1000 -> "Monbat";
            case 1100 -> "Start";
            default -> "";
        };

        // Check for VRLA
        plantName = switch (StringUtils.left(salesOrderItem.getMaterial(), 4)) {
            case "1012", "102M", "104M", "106M", "108H" -> "RP";
            default -> plantName;
        };
        return plantName;
    }
}
