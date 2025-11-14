package com.monbat.planning.controllers;

import com.monbat.planning.services.CapacityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sap")
public class CapacityController {
    @Autowired
    private CapacityService capacityService;

    private static final Logger logger = LoggerFactory.getLogger(CapacityController.class);

    @RequestMapping(value = "/getCapacity", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCapacity(@RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam String manufacturingOrder,
                                         @RequestParam boolean isProductionOrder,
                                         @RequestParam LocalDateTime scheduleTime) {
        try {
            LocalDateTime freeCapacity = this.capacityService.getFreeCapacity(
                    username, password, manufacturingOrder, isProductionOrder, scheduleTime);

            return ResponseEntity.ok(freeCapacity);

        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
