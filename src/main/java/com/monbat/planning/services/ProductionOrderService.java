package com.monbat.planning.services;

import com.monbat.planning.models.production_order.ProductionOrderDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductionOrderService {
    List<ProductionOrderDto> getProductionOrders(String username, String password,
                                                 LocalDateTime reqDelDateBegin,
                                                 LocalDateTime reqDelDateEnd);
}
