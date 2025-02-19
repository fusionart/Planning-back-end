package com.monbat.planning.services;

import com.monbat.planning.models.entities.ProductionSummary;
import com.monbat.planning.repositories.ProductionSummaryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductionSummaryService {
    void importProductionSummary(ProductionSummaryRepository productionSummaryRepository,
                             ModelMapper modelMapper, MultipartFile file);
    void startImportProductionSummary(MultipartFile file);
    Map<String, Map<String, Double>> sumWorkCenterQuantities();
    List<String> getDistinctReqDelDate();
    List<ProductionSummary> getAllProductionSummary();
}
