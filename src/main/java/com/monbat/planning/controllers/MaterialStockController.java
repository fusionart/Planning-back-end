package com.monbat.planning.controllers;

import com.monbat.planning.services.MaterialStockService;
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

@RestController
@RequestMapping("/api/sap")
public class MaterialStockController implements Serializable {
    @Autowired
    private MaterialStockService materialStockService;

    private static final Logger logger = LoggerFactory.getLogger(MaterialStockController.class);

    @RequestMapping(value = "/getMaterialStock", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMaterialStock(@RequestParam String username,
                                            @RequestParam String password,
                                            @RequestParam String material) {
        try {
            return ResponseEntity.ok(this.materialStockService.getMaterialStock(username, password, material));
        } catch (Exception e) {
            logger.error("Error in getSalesOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
