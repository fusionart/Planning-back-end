package com.monbat.planning.services;

import com.monbat.planning.models.production_order.ProductionOrderDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductionOrderService {
    List<ProductionOrderDto> getProductionOrders(String username, String password,
                                                 LocalDateTime reqDelDateBegin,
                                                 LocalDateTime reqDelDateEnd);


    List<ProductionOrderDto> getProductionOrdersByMaterial(String username, String password,
                                                 String material,
                                                 LocalDateTime reqDelDateBegin,
                                                 LocalDateTime reqDelDateEnd);

    List<ProductionOrderDto> getProductionOrdersByProductionSupervisor(String username, String password,
                                                           String productionSupervisor,
                                                           LocalDateTime reqDelDateBegin,
                                                           LocalDateTime reqDelDateEnd);

    String convertPlannedOrder(String username, String password,
                             String plannedOrder,
                             String manufacturingOrderType);

    void updateProductionOrder(String username, String password,
                             String productionOrder, LocalDateTime scheduledStartDateTime, boolean schedule);

    ProductionOrderDto getProductionOrder(String username, String password,
                                          String productionOrder);

    String createProductionOrder(String username, String password,
                                 String material,
                                 String productionPlant,
                                 String manufacturingOrderType,
                                 String totalQuantity,
                                 String productionVersion);

    void updateStorageLocation(String username, String password,
                               String manufacturingOrder, String newStorageLocation);

    void updateProductionVersion(String username, String password,
                               String manufacturingOrder, String productionVersion);

    void updateProductionOrderQuantity(String username, String password,
                                       String productionOrder, BigDecimal quantity);
}
