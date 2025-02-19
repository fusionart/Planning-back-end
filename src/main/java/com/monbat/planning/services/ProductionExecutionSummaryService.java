package com.monbat.planning.services;

import com.monbat.planning.models.other.ProductionExecutionSummary;

import java.util.List;

public interface ProductionExecutionSummaryService {
    List<ProductionExecutionSummary> calculateProductionExecutionSummary();
}
