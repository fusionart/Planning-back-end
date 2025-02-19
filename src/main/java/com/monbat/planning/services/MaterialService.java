package com.monbat.planning.services;

import com.monbat.planning.models.entities.Material;
import com.monbat.planning.repositories.MaterialRepository;
import org.modelmapper.ModelMapper;

import java.io.File;
import java.util.List;

public interface MaterialService {
    void importMaterials(MaterialRepository materialRepository, ModelMapper modelMapper, File file, String uploadId);
    void startImportMaterial(File file, String uploadId);
    Material getMaterialByCode(String materialCode);
    List<Material> getAllMaterials();
    List<Material> getAllMaterialsByCurringTimeNotOrLeadTimeOffsetNot(int curringTime, int leadTimeOffset);
    List<Material> getAllMaterialsByKilosForEachNot(int kilosForEach);
    int getProgress(String uploadId);
}
