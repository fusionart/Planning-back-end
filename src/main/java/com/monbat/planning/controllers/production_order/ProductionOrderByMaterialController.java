package com.monbat.planning.controllers.production_order;

import com.monbat.planning.models.production_order.ProductionOrderDto;
import com.monbat.planning.services.ProductionOrderByMaterialService;
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

@RestController
@RequestMapping("/api/sap")
public class ProductionOrderByMaterialController implements Serializable {
    @Autowired
    private ProductionOrderByMaterialService productionOrderByMaterialService;

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProductionOrderByMaterialController.class);

    @RequestMapping(value = "/getProductionOrdersByMaterial", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProductionOrders(@RequestParam String username,
                                                 @RequestParam String password,
                                                 @RequestParam String material,
                                                 @RequestParam LocalDateTime reqDelDateBegin,
                                                 @RequestParam LocalDateTime reqDelDateEnd) {
        try {
            List<ProductionOrderDto> productionOrders = this.productionOrderByMaterialService.getProductionOrders(
                    username, password, material, reqDelDateBegin, reqDelDateEnd);

            logger.info("Successfully retrieved {} production orders", productionOrders.size());
            return ResponseEntity.ok(productionOrders);

        } catch (Exception e) {
            logger.error("Error in getProductionOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
