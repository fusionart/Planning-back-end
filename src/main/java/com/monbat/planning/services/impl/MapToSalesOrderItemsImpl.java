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

    private List<SalesOrderByDate> generateSalesOrderMainData(List<SalesOrderHeader> salesOrderHeaders, String username,
                                                           String password, LocalDateTime reqDelDateBegin, LocalDateTime reqDelDateEnd) {

        List<SalesOrderByDate> salesOrderByDateList = new ArrayList<>();
        List<SalesOrderMain> salesOrderMainList;
        List<Material> materialList = (List<Material>) this.materialController.getMaterials();

        List<PlannedOrderDto> plannedOrderList = this.plannedOrderService.getPlannedOrders(username, password,
                reqDelDateBegin, reqDelDateEnd);
        List<ProductionOrderDto> productionOrderList = this.productionOrderService.getProductionOrders(username,
                password, reqDelDateBegin, reqDelDateBegin);

        for (SalesOrderHeader salesOrderHeader : salesOrderHeaders) {
            assert salesOrderHeader.getRequestedDeliveryDate() != null;
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
                    double finalBatteryQuantity =
                            this.materialStockService.getMaterialStock(username, password,
                                    "10" + StringUtils.right(salesOrderItem.getMaterial(),
                                            salesOrderItem.getMaterial().length() - 2));

                    assert salesOrderItem.getRequestedQuantity() != null;
                    SalesOrderMain salesOrderMain = new SalesOrderMain(salesOrderItem.getMaterial(),
                            salesOrderItem.getRequestedQuantity().doubleValue(), plantName,
                            salesOrderItem.getRequestedQtyUnit(),
                            notChargedQuantity,
                            chargedQuantity,
                            finalBatteryQuantity,
                            salesOrderItem.getRequestedQuantity().doubleValue());

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

    public List<SalesOrderByDate> calculateCumulativeValues(List<SalesOrderHeader> salesOrderHeaders, String username,
                      String password, LocalDateTime reqDelDateBegin, LocalDateTime reqDelDateEnd){
        List<SalesOrderByDate> salesOrderByDateList = generateSalesOrderMainData(salesOrderHeaders, username,
                password, reqDelDateBegin, reqDelDateEnd);

        salesOrderByDateList.sort((o1, o2) -> {
            String[] p1 = o1.getReqDlvWeek().split("/");
            String[] p2 = o2.getReqDlvWeek().split("/");

            int yearCompare = Integer.compare(Integer.parseInt(p1[1]), Integer.parseInt(p2[1]));
            if (yearCompare != 0) return yearCompare;
            return Integer.compare(Integer.parseInt(p1[0]), Integer.parseInt(p2[0]));
        });

        int k = 1;
        for (int i = 0; i < salesOrderByDateList.size() - 1; i++) {

            for (int j = 0; j < k; j++) {
                List<SalesOrderMain> previousList =  salesOrderByDateList.get(j).getSalesOrderMainList();
                List<SalesOrderMain> nextList =  salesOrderByDateList.get(k).getSalesOrderMainList();

                for (SalesOrderMain salesOrderMain : nextList){
                    String material = salesOrderMain.getMaterial();

                    Double quantity = previousList.stream()
                            .filter(order -> material.equals(order.getMaterial()))
                            .findFirst()
                            .map(SalesOrderMain::getCumulativeQuantity)
                            .orElse(0.0);

                    salesOrderMain.setCumulativeQuantity(quantity + salesOrderMain.getRequestedQuantity());
                }
            }
            k++;
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
            case null, default -> plantName;
        };
        return plantName;
    }
}
