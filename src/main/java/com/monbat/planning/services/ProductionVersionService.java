package com.monbat.planning.services;

import com.monbat.planning.models.entities.ProductionVersion;

import java.io.File;
import java.util.List;

public interface ProductionVersionService {
    boolean areImported();
    void importProductionVersions(File file, String uploadId);
    List<ProductionVersion> getAllProductionVersions();
    List<ProductionVersion> getProductionVersionsByMaterialAndPlant(String material, int plant);
    ProductionVersion getMaterialAndProductionVersionNumber(String material, int productionVersionNumber);
    int getProgress(String uploadId);
    void startImportProductionVersions(File file, String uploadId);
    void loadProductionVersionsFromSap(String username, String password);
}
