package com.monbat.planning.controllers;

import com.monbat.planning.models.entities.ProductionVersion;
import com.monbat.planning.services.ProductionVersionService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sap")
public class ProductionVersionController {
    @Autowired
    private ProductionVersionService productionVersionService;

    private static final Logger logger = LoggerFactory.getLogger(ProductionVersionController.class);

    @RequestMapping(value = "/importProductionVersions", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> importProductionVersions(@RequestParam String username,
                                            @RequestParam String password) {
        try {
            this.productionVersionService.loadProductionVersionsFromSap(username, password);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Production versions updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in getSalesOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/getProductionVersionByMaterial", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProductionVersionByMaterial(@RequestParam String material, @RequestParam String plant) {
        try {
            List<ProductionVersion> productionVersions =
                    this.productionVersionService.getProductionVersionsByMaterialAndPlant(material, Integer.parseInt(plant));

            return ResponseEntity.ok(productionVersions);

        } catch (Exception e) {
            logger.error("Error in getSalesOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
