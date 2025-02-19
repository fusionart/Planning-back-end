package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.ProductionVersionDto;
import com.monbat.planning.models.entities.ProductionVersion;
import com.monbat.planning.repositories.ProductionVersionRepository;
import com.monbat.planning.services.ProductionVersionService;
import com.monbat.planning.utils.ImportTypes;
import com.monbat.planning.utils.poi.ReadExcelFile;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.monbat.planning.services.utils.columns.ProductionVersionFileColumns.*;

@Service
public class ProductionVersionServiceImpl implements ProductionVersionService {
    @Autowired
    private ProductionVersionRepository productionVersionRepository;
    @Autowired
    private ModelMapper modelMapper;

    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    @Override
    public boolean areImported() {
        return this.productionVersionRepository.count() > 0;
    }

    @Override
    public void importProductionVersions(File file, String uploadId) {
        if (file != null) {
            XSSFSheet sheet = ReadExcelFile.getWorksheet((File) file);
            //Clear the table before new import
            this.productionVersionRepository.truncateTable();

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || row.getRowNum() == 1) {
                    continue;
                }

                ProductionVersionDto productionVersionDto = new ProductionVersionDto();

                productionVersionDto.setMaterial(row.getCell(MATERIAL_COLUMN).getStringCellValue());
                productionVersionDto.setPlant(Integer.parseInt(row.getCell(PLANT_COLUMN).getStringCellValue()));
                productionVersionDto.setProductionVersionNumber(Integer.parseInt(row.getCell(PRODUCTION_VERSION_NUMBER_COLUMN).getStringCellValue()));
                productionVersionDto.setRoutingGroup(row.getCell(ROUTING_GROUP_COLUMN).getStringCellValue());
                productionVersionDto.setRoutingGroupCounter(Integer.parseInt(row.getCell(ROUTING_GROUP_COUNTER_COLUMN).getStringCellValue()));
                productionVersionDto.setDescription(row.getCell(DESCRIPTION_COLUMN).getStringCellValue());

                this.productionVersionRepository.save(modelMapper.map(productionVersionDto, ProductionVersion.class));

                progressMap.put(uploadId, (row.getRowNum() * 100) / (sheet.getLastRowNum() - 1));
            }
        }
        progressMap.put(uploadId, 100);
    }

    @Override
    public int getProgress(String uploadId) {
        return progressMap.getOrDefault(uploadId, 0);
    }

    @Override
    public void startImportProductionVersions(File file, String uploadId) {
        importProductionVersions(file, uploadId);
    }

    @Override
    public List<ProductionVersion> getAllProductionVersions() {
        return this.productionVersionRepository.findAll().stream().toList();
    }

    @Override
    public ProductionVersion getMaterialAndProductionVersionNumber(String material, int productionVersionNumber) {
        return this.productionVersionRepository.findFirstByMaterialAndProductionVersionNumber(material, productionVersionNumber);
    }
}
