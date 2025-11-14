package com.monbat.planning.services;

import java.time.LocalDateTime;

public interface CapacityService {
    LocalDateTime getFreeCapacity(String username,
                         String password,
                         String manufacturingOrder,
                         boolean isProductionOrder,
                         LocalDateTime scheduleTime);
}
