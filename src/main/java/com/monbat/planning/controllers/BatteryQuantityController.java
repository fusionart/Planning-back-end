package com.monbat.planning.controllers;

import com.monbat.planning.models.entities.BatteryQuantity;
import com.monbat.planning.services.BatteryQuantityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calculations")
public class BatteryQuantityController {
    @Autowired
    private BatteryQuantityService batteryQuantityService;

    @GetMapping("/quantityByLocation")
    public ResponseEntity<List<BatteryQuantity>> getAllBatteryQuantityByStorageLocation(@RequestParam Integer param1) {
        return ResponseEntity.ok(this.batteryQuantityService.getAllByStorageLocation(param1));
    }

    @GetMapping("/quantityByBatteryCodePrefix")
    public ResponseEntity<List<BatteryQuantity>> getAllBatteryQuantityByCodeBegin(@RequestParam Integer param1) {
        return ResponseEntity.ok(this.batteryQuantityService.getAllByBatteryCodePrefix(param1));
    }
}
