package com.monbat.planning.controllers;

import com.monbat.planning.models.entities.ProductionSupervisor;
import com.monbat.planning.models.entities.WorkCenter;
import com.monbat.planning.services.ProductionSupervisorService;
import com.monbat.planning.services.WorkCenterService;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sap")
public class ProductionSupervisorController implements Serializable {
    @Autowired
    private ProductionSupervisorService productionSupervisorService;
    @Autowired
    private WorkCenterService workCenterService;

    private static final Logger logger = LoggerFactory.getLogger(ProductionSupervisorController.class);

    @RequestMapping(value = "/getProductionSupervisor", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProductionSupervisor() {
        try {
            List<ProductionSupervisor> productionOrders = this.productionSupervisorService.getProductionSupervisors();

            logger.info("Successfully retrieved {} production supervisor", Optional.of(productionOrders.size()));
            return ResponseEntity.ok(productionOrders);

        } catch (Exception e) {
            logger.error("Error in getProductionOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/getWorkCentersByProductionSupervisor", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getWorkCentersByProductionSupervisor(@RequestParam String productionSupervisor) {
        try {
            logger.info("Fetching work centers for supervisor: {}", productionSupervisor);

            List<WorkCenter> workCenters = workCenterService
                    .findByProductionSupervisor(productionSupervisor);

            logger.info("Found {} work centers for supervisor: {}",
                    workCenters.size(), productionSupervisor);

            return ResponseEntity.ok(workCenters);
        } catch (Exception e) {
            logger.error("Error fetching work centers for supervisor {}: ",
                    productionSupervisor, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching work centers: " + e.getMessage());
        }
    }
}
