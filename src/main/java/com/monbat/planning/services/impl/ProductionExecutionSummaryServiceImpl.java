package com.monbat.planning.services.impl;

import com.monbat.planning.models.entities.ProductionSummary;
import com.monbat.planning.models.entities.WorkCenterCapacity;
import com.monbat.planning.models.other.ProductionExecutionSummary;
import com.monbat.planning.services.ProductionExecutionSummaryService;
import com.monbat.planning.services.ProductionSummaryService;
import com.monbat.planning.services.WorkCenterCapacityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductionExecutionSummaryServiceImpl implements ProductionExecutionSummaryService {
    @Autowired
    private ProductionSummaryService productionSummaryService;
    @Autowired
    private WorkCenterCapacityService workCenterCapacityService;


    @Override
    public List<ProductionExecutionSummary> calculateProductionExecutionSummary() {
        List<ProductionSummary> productionSummaryList = this.productionSummaryService.getAllProductionSummary();
        List<WorkCenterCapacity> workCenterCapacityList = this.workCenterCapacityService.getAllWorkCenters();

        List<ProductionExecutionSummary> productionExecutionSummaryList = new ArrayList<>();

        for (ProductionSummary productionSummary : productionSummaryList){
            
        }




        return List.of();
    }
}
