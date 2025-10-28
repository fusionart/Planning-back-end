package com.monbat.planning.controllers;

import com.monbat.planning.models.entities.ProductionSupervisor;
import com.monbat.planning.services.ProductionSupervisorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sap")
public class ProductionSupervisorController implements Serializable {
    @Autowired
    private ProductionSupervisorService productionSupervisorService;

    private static final Logger logger = LoggerFactory.getLogger(ProductionSupervisorController.class);

    @RequestMapping(value = "/getProductionSupervisor", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProductionSupervisor() {
        try {
            List<ProductionSupervisor> productionOrders = this.productionSupervisorService.getProductionSupervisors();

            logger.info("Successfully retrieved {} production orders", Optional.of(productionOrders.size()));
            return ResponseEntity.ok(productionOrders);

        } catch (Exception e) {
            logger.error("Error in getProductionOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
