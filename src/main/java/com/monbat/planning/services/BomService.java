package com.monbat.planning.services;

import com.monbat.planning.models.entities.Bom;
import com.monbat.planning.repositories.BomRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface BomService {
    void importBoms(File file, String uploadId);
    void startImportBom(File file, String uploadId);
    List<Bom> getBomByMaterialForAssembly(String material);
    List<Bom> getWholeBomByMaterial(String material);
    List<Bom> getAllBoms();
    int getProgress(String uploadId);
}
