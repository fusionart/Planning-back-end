package com.monbat.planning.services;

import com.monbat.planning.models.entities.Readiness;
import com.monbat.planning.repositories.ReadinessRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ReadinessService {
    void importReadiness(File file, String uploadId);
    void startImportReadiness(File file, String uploadId);
    List<Readiness> findAllReadiness();
    List<String> getDistinctReqDelDate();
    List<String> getDistinctReadinessWeek();
    List<Readiness> getAllByByWeekOfReadiness(String weekOfReadiness, int productionPlan);
    Map<String, Integer> sumMaterialQuantitiesByReqDlvWeek(String reqDlvWeek, int productionPlant);
    int getProgress(String uploadId);
}
