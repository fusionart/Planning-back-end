package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.BomDto;
import com.monbat.planning.models.entities.Bom;
import com.monbat.planning.repositories.BomRepository;
import com.monbat.planning.services.BomService;
import com.monbat.planning.services.utils.FindColumnIndexFromExcelRow;
import com.monbat.planning.services.utils.ShowAlert;
import com.monbat.planning.utils.poi.ReadExcelFile;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.monbat.planning.services.utils.columns.BomColumns.*;
import static com.monbat.planning.utils.constants.Messages.SUCCESSFUL_IMPORT;

@Service
public class BomServiceImpl implements BomService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private BomRepository bomRepository;

    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    @Override
    public void importBoms(File file, String uploadId) {
        if (file != null) {
            this.bomRepository.truncateTable();

            XSSFSheet sheet = ReadExcelFile.getWorksheet(file);

            Row headerRow = sheet.getRow(0);

            int productionPlantColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, PRODUCTION_PLANT);
            int materialColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MATERIAL);
            int baseQuantityColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, BASE_QUANTITY);
            int componentColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, COMPONENT);
            int quantityColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, QUANTITY);
            int componentUomColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, COMPONENT_UOM);


            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                BomDto bomDto = new BomDto();
                bomDto.setPlant(Integer.parseInt(row.getCell(productionPlantColumn).getStringCellValue()));
                bomDto.setMaterial(row.getCell(materialColumn).getStringCellValue());
                bomDto.setBaseQuantity((int) row.getCell(baseQuantityColumn).getNumericCellValue());
                bomDto.setComponent(row.getCell(componentColumn).getStringCellValue());
                bomDto.setQuantity(row.getCell(quantityColumn).getNumericCellValue());
                bomDto.setComponentUom(row.getCell(componentUomColumn).getStringCellValue());

                bomRepository.save(modelMapper.map(bomDto, Bom.class));

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
    public void startImportBom(File file, String uploadId) {
        importBoms(file, uploadId);
    }

    @Override
    public List<Bom> getBomByMaterialForAssembly(String material) {
        List<Bom> boms = this.bomRepository.findAllByMaterial(material);

        return boms.stream()
                .filter(bom -> bom.getComponent().startsWith("20"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Bom> getWholeBomByMaterial(String material) {
        return this.bomRepository.findAllByMaterial(material);
    }

    @Override
    public List<Bom> getAllBoms() {
        return this.bomRepository.findAll();
    }
}
