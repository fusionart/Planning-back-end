package com.monbat.planning.controllers;

import com.monbat.planning.models.other.ReadinessByWeek;
import com.monbat.planning.services.calculations.CalculatePlan10sService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calculations")
public class CalculatePlan10sController {
    @Autowired
    private CalculatePlan10sService calculatePlan10sService;

    @GetMapping("/plan10s")
    public ResponseEntity<List<ReadinessByWeek>> getProgress() {
        return ResponseEntity.ok(this.calculatePlan10sService.getReadinessByWeek());
    }
}
