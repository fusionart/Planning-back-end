package com.monbat.planning.controllers.production_order;

import com.monbat.planning.models.production_order.ProductionOrderDto;
import com.monbat.planning.services.ProductionOrderService;
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

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sap")
public class ProductionOrderController implements Serializable {

    @Autowired
    private ProductionOrderService productionOrderService;

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProductionOrderController.class);

    /**
     * Get production orders within the specified date range
     */
    @RequestMapping(value = "/getProductionOrders", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProductionOrders(@RequestParam String username,
                                                 @RequestParam String password,
                                                 @RequestParam LocalDateTime reqDelDateBegin,
                                                 @RequestParam LocalDateTime reqDelDateEnd) {
        try {
            logger.info("Received request for production orders from {} to {}", reqDelDateBegin, reqDelDateEnd);

            List<ProductionOrderDto> productionOrders = this.productionOrderService.getProductionOrders(
                    username, password, reqDelDateBegin, reqDelDateEnd);

            logger.info("Successfully retrieved {} production orders", Optional.of(productionOrders.size()));
            return ResponseEntity.ok(productionOrders);

        } catch (Exception e) {
            logger.error("Error in getProductionOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/convertPlannedOrder", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> convertPlannedOrder(@RequestParam String username,
                                                 @RequestParam String password,
                                                 @RequestParam String plannedOrder,
                                                 @RequestParam String manufacturingOrderType) {
        try {
            logger.info("Converting planned order {} with manufacturing order type {}",
                    plannedOrder, manufacturingOrderType);

            String productionOrderNumber = this.productionOrderService.convertPlannedOrder(
                    username, password, plannedOrder, manufacturingOrderType);

            logger.info("Successfully converted planned order {} to production order {}",
                    plannedOrder, productionOrderNumber);

            return ResponseEntity.ok(productionOrderNumber);

        } catch (Exception e) {
            logger.error("Error in convertPlannedOrder: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/updateProductionOrder", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProductionOrder(@RequestParam String username,
                                                   @RequestParam String password,
                                                   @RequestParam String productionOrder,
                                                   @RequestParam LocalDateTime scheduledStartDateTime) {
        try {
            logger.info("Updating production order {} with scheduled start date/time {}",
                    productionOrder, scheduledStartDateTime);

            this.productionOrderService.updateProductionOrder(
                    username, password, productionOrder, scheduledStartDateTime);

            logger.info("Successfully updated production order {}", productionOrder);

            // Return JSON response instead of plain text
            Map<String, String> response = new HashMap<>();
            response.put("message", "Production order updated successfully");
            response.put("productionOrder", productionOrder);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in updateProductionOrder: ", e);

            // Return JSON error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @RequestMapping(value = "/createProductionOrder", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createProductionOrder(@RequestParam String username,
                                                   @RequestParam String password,
                                                   @RequestParam String material,
                                                   @RequestParam String productionPlant,
                                                   @RequestParam String manufacturingOrderType,
                                                   @RequestParam String totalQuantity) {
        try {
            logger.info("Creating production order for material {} in plant {} with quantity {}",
                    material, productionPlant, totalQuantity);

            String productionOrderNumber = this.productionOrderService.createProductionOrder(
                    username, password, material, productionPlant, manufacturingOrderType, totalQuantity);

            logger.info("Successfully created production order: {}", productionOrderNumber);

            return ResponseEntity.ok(productionOrderNumber);

        } catch (Exception e) {
            logger.error("Error in createProductionOrder: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/updateStorageLocation", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateStorageLocation(@RequestParam String username,
                                                   @RequestParam String password,
                                                   @RequestParam String manufacturingOrder,
                                                   @RequestParam String newStorageLocation) {
        try {
            this.productionOrderService.updateStorageLocation(
                    username, password, manufacturingOrder, newStorageLocation);

            return ResponseEntity.ok("ok");

        } catch (Exception e) {
            logger.error("Error in createProductionOrder: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
