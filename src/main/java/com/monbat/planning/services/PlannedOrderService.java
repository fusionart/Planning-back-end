package com.monbat.planning.services;

import com.monbat.planning.models.dto.PlannedOrderDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PlannedOrderService {
    List<PlannedOrderDto> getPlannedOrders(String username, String password,
                                           LocalDateTime reqDelDateBegin,
                                           LocalDateTime reqDelDateEnd);

    PlannedOrderDto getPlannedOrder(String username, String password,
                                    String plannedOrder);

    void updatePlannedOrder(String username, String password, String plannedOrder, boolean plannedOrderCapacityIsDsptchd,
                            String opLtstSchedldProcgStrtDte,
                            String opLtstSchedldProcgStrtTme,
                            String opLtstSchedldTrdwnStrtDte,
                            String opLtstSchedldTrdwnStrtTme);
}
