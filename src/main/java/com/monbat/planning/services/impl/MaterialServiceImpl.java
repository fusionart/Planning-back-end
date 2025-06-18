package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.MaterialDto;
import com.monbat.planning.models.entities.Material;
import com.monbat.planning.repositories.MaterialRepository;
import com.monbat.planning.services.MaterialService;
import com.monbat.planning.services.utils.FindColumnIndexFromExcelRow;
import com.monbat.planning.services.utils.ShowAlert;
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

import static com.monbat.planning.services.utils.columns.MaterialColumns.*;
import static com.monbat.planning.utils.constants.Messages.SUCCESSFUL_IMPORT;

@Service
public class MaterialServiceImpl implements MaterialService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MaterialRepository materialRepository;

    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    @Override
    public void importMaterials(MaterialRepository materialRepository, ModelMapper modelMapper, File file, String uploadId) {
        if (file != null) {
            this.materialRepository.truncateTable();

            XSSFSheet sheet = ReadExcelFile.getWorksheet(file);

            Row headerRow = sheet.getRow(0);

            int productionPlantColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, PRODUCTION_PLANT);
            int materialColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MATERIAL);
            int materialDescriptionColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, DESCRIPTION);
            int materialTypeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MATERIAL_TYPE);
            int materialGroupColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MATERIAL_GROUP);
            int uomColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, UOM);
            int externalMaterialGroupColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, EXTERNAL_MATERIAL_GROUP);
//            int leadTimeOffsetColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, LEAD_TIME_OFFSET);
//            int curringTimeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, CURRING);
            int kilosForEachColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, KILOS_FOR_EACH);

            int processedRows = 0;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                MaterialDto materialDto = new MaterialDto();
                materialDto.setMaterial(row.getCell(materialColumn).getStringCellValue());
                materialDto.setPlant(Integer.parseInt(row.getCell(productionPlantColumn).getStringCellValue()));
                materialDto.setDescription(row.getCell(materialDescriptionColumn).getStringCellValue());
                materialDto.setMaterialType(row.getCell(materialTypeColumn).getStringCellValue());
                materialDto.setMaterialGroup(row.getCell(materialGroupColumn).getStringCellValue());
                materialDto.setUom(row.getCell(uomColumn).getStringCellValue());
                materialDto.setExternalMaterialGroup(row.getCell(externalMaterialGroupColumn).getStringCellValue());
                materialDto.setLeadTimeOffset(0);
                materialDto.setCurringTime(0);
                materialDto.setKilosForEach((int) row.getCell(kilosForEachColumn).getNumericCellValue());

                materialRepository.save(modelMapper.map(materialDto, Material.class));

                progressMap.put(uploadId, (row.getRowNum() * 100) / (sheet.getLastRowNum() - 1));
            }
        }
        progressMap.put(uploadId, 100);
    }

    @Override
    public void startImportMaterial(File file, String uploadId) {
        importMaterials(this.materialRepository, this.modelMapper, file, uploadId);
        ShowAlert.showAlert(SUCCESSFUL_IMPORT);
    }

    @Override
    public Material getMaterialByCode(String materialCode) {
        return this.materialRepository.findFirstByMaterial(materialCode);
    }

    @Override
    public List<Material> getAllMaterials() {
        return this.materialRepository.findAll().stream().toList();
    }

    @Override
    public List<Material> getAllMaterialsByCurringTimeNotOrLeadTimeOffsetNot(int curringTime, int leadTimeOffset) {
        return this.materialRepository.findByCurringTimeNotOrLeadTimeOffsetNot(curringTime, leadTimeOffset);
    }

    @Override
    public List<Material> getAllMaterialsByKilosForEachNot(int kilosForEach) {
        return this.materialRepository.findByKilosForEachNot(kilosForEach);
    }

    @Override
    public int getProgress(String uploadId) {
        return progressMap.getOrDefault(uploadId, 0);
    }
}
