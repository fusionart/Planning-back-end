package com.monbat.planning.controllers;

import com.monbat.planning.services.BusinessPartnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public class BusinessPartnerController {
    @Autowired
    private BusinessPartnerService businessPartnerService;

    private static final Logger logger = LoggerFactory.getLogger(BusinessPartnerController.class);

    @RequestMapping(value = "/getBusinessPartner", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBusinessPartner(@RequestParam String username,
                                                 @RequestParam String password,
                                                 @RequestParam String businessPartnerNumber) {
        try {
            String name = this.businessPartnerService.getBusinessPartnerName(
                    username, password, businessPartnerNumber);

            return ResponseEntity.ok(name);

        } catch (Exception e) {
            logger.error("Error in getProductionOrders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
