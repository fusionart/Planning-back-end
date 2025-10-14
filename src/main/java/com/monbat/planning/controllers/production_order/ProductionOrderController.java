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
import java.util.List;
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
    public void convertPlannedOrder (@RequestParam String username,
                                     @RequestParam String password,
                                     @RequestParam String plannedOrder,
                                     @RequestParam String manufacturingOrderType) {

        this.productionOrderService.convertPlannedOrder(username, password, plannedOrder, manufacturingOrderType);
    }

    @RequestMapping(value = "/updateProductionOrder", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateProductionOrder (@RequestParam String username,
                                     @RequestParam String password,
                                     @RequestParam String productionOrder) {

        this.productionOrderService.updateProductionOrder(username, password, productionOrder);
    }
}
