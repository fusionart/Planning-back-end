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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sap")
public class PlannedOrderController implements Serializable {

    @Autowired
    private PlannedOrderService plannedOrderService;

    private static final Logger logger = LoggerFactory.getLogger(PlannedOrderController.class);

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

    @RequestMapping(value = "/dispatchPlannedOrder", method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dispatchPlannedOrder(@RequestParam String username,
                                                @RequestParam String password,
                                                @RequestParam String plannedOrder,
                                                @RequestParam LocalDateTime dispatchTime) {
        try {
            this.plannedOrderService.dispatchPlannedOrder(username, password, plannedOrder, dispatchTime);

            Map<String, String> response = new HashMap<>();
            response.put("message", "OK");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/deallocatePlannedOrder", method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deallocatePlannedOrder(@RequestParam String username,
                                                  @RequestParam String password,
                                                  @RequestParam String plannedOrder,
                                                  @RequestParam LocalDateTime dispatchTime) {
        try {
            this.plannedOrderService.deallocatePlannedOrder(username, password, plannedOrder, dispatchTime);

            Map<String, String> response = new HashMap<>();
            response.put("message", "OK");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/updatePlannedOrderQuantity", method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePlannedOrderQuantity(@RequestParam String username,
                                                  @RequestParam String password,
                                                  @RequestParam String plannedOrder,
                                                  @RequestParam BigDecimal quantity) {
        try {
            this.plannedOrderService.updatePlannedOrderQuantity(username, password, plannedOrder, quantity);

            Map<String, String> response = new HashMap<>();
            response.put("message", "OK");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/updatePlannedOrderProductionVersion", method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePlannedOrderProductionVersion(@RequestParam String username,
                                                @RequestParam String password,
                                                @RequestParam String plannedOrder,
                                                @RequestParam String productionVersion) {
        try {
            this.plannedOrderService.updatePlannedOrderProductionVersion(username, password, plannedOrder, productionVersion);

            Map<String, String> response = new HashMap<>();
            response.put("message", "OK");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in getPlannedOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
