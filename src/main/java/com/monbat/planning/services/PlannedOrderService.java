package com.monbat.planning.services;

import com.monbat.planning.models.dto.PlannedOrderDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PlannedOrderService {
    List<PlannedOrderDto> getPlannedOrders(String username, String password,
                                           LocalDateTime reqDelDateBegin,
                                           LocalDateTime reqDelDateEnd);
}
