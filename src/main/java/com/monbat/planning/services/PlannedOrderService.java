package com.monbat.planning.services;

import com.monbat.planning.models.dto.PlannedOrderDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PlannedOrderService {
    List<PlannedOrderDto> getPlannedOrders(String username, String password,
                                           LocalDateTime reqDelDateBegin,
                                           LocalDateTime reqDelDateEnd);

    List<PlannedOrderDto> getPlannedOrdersByProductionSupervisor(String username, String password,
                                           String productionSupervisor,
                                           LocalDateTime reqDelDateBegin,
                                           LocalDateTime reqDelDateEnd);

    PlannedOrderDto getPlannedOrder(String username, String password,
                                    String plannedOrder);

    void dispatchPlannedOrder(String username, String password, String plannedOrder,
                            LocalDateTime opLtstSchedldProcgStrtDteTme);

    void deallocatePlannedOrder(String username, String password, String plannedOrder,
                              LocalDateTime opLtstSchedldProcgStrtDteTme);

    void updatePlannedOrder(String username, String password, String plannedOrder,
                            String productionVersion, BigDecimal totalQuantity);
}
