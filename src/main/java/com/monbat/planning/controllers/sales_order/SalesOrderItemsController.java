package com.monbat.planning.controllers.sales_order;

import com.monbat.planning.models.sales_order.SalesOrderByDate;
import com.monbat.planning.services.SalesOrderService;
import com.monbat.planning.services.impl.MapToSalesOrderItemsImpl;
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
public class SalesOrderItemsController implements Serializable {

    @Autowired
    private SalesOrderService salesOrderService;
    @Autowired
    private MapToSalesOrderItemsImpl mapToSalesOrderItems;

    private static final Logger logger = LoggerFactory.getLogger(SalesOrderItemsController.class);

    /**
     * Get sales orders with items within the specified date range
     * This endpoint returns sales orders with expanded items (same as original functionality)
     */
    @RequestMapping(value = "/getSalesOrdersItems", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSalesOrders(@RequestParam String username,
                                            @RequestParam String password,
                                            @RequestParam LocalDateTime reqDelDateBegin,
                                            @RequestParam LocalDateTime reqDelDateEnd) {
        try {
            logger.info("Received request for sales orders from {} to {}", reqDelDateBegin, reqDelDateEnd);

            List<SalesOrderByDate> salesOrders =
                    this.mapToSalesOrderItems.generateSalesOrderMainData(salesOrderService.getSalesOrdersItems(
                    username, password, reqDelDateBegin, reqDelDateEnd), username, password, reqDelDateBegin, reqDelDateEnd);

            logger.info("Successfully retrieved {} sales orders", salesOrders.size());
            return ResponseEntity.ok(salesOrders);

        } catch (Exception e) {
            logger.error("Error in getSalesOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
