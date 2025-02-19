package com.monbat.planning.services;

import com.monbat.planning.models.entities.ProductionVersion;
import com.monbat.planning.utils.ImportTypes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface ProductionVersionService {
    boolean areImported();
    void importProductionVersions(File file, String uploadId);
    List<ProductionVersion> getAllProductionVersions();
    ProductionVersion getMaterialAndProductionVersionNumber(String material, int productionVersionNumber);
    int getProgress(String uploadId);
    void startImportProductionVersions(File file, String uploadId);
}
