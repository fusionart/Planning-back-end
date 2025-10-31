package com.monbat.planning.controllers.planned_order;

import com.monbat.planning.models.dto.PlannedOrderDto;
import com.monbat.planning.services.PlannedOrderService;
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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sap")
public class PlannedOrderController implements Serializable {

    @Autowired
    private PlannedOrderService plannedOrderService;

    private static final Logger logger = LoggerFactory.getLogger(PlannedOrderController.class);

    /**
     * Get planned orders within the specified date range
     */
    @RequestMapping(value = "/getPlannedOrders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlannedOrders(@RequestParam String username,
                                              @RequestParam String password,
                                              @RequestParam LocalDateTime reqDelDateBegin,
                                              @RequestParam LocalDateTime reqDelDateEnd) {
        try {
            logger.info("Received request for planned orders from {} to {}", reqDelDateBegin, reqDelDateEnd);

            List<PlannedOrderDto> plannedOrders = this.plannedOrderService.getPlannedOrders(
                    username, password, reqDelDateBegin, reqDelDateEnd);

            logger.info("Successfully retrieved {} planned orders", plannedOrders.size());
            return ResponseEntity.ok(plannedOrders);

        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/getPlannedOrdersByProductionSupervisor", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlannedOrdersByProductionSupervisor(@RequestParam String username,
                                              @RequestParam String password,
                                              @RequestParam String productionSupervisor,
                                              @RequestParam LocalDateTime reqDelDateBegin,
                                              @RequestParam LocalDateTime reqDelDateEnd) {
        try {
            logger.info("Received request for planned orders from {} to {}", reqDelDateBegin, reqDelDateEnd);

            List<PlannedOrderDto> plannedOrders = this.plannedOrderService.getPlannedOrdersByProductionSupervisor(
                    username, password, productionSupervisor, reqDelDateBegin, reqDelDateEnd);

            logger.info("Successfully retrieved {} planned orders", plannedOrders.size());
            return ResponseEntity.ok(plannedOrders);

        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
