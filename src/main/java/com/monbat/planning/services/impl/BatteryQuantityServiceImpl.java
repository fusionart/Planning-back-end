package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.BatteryQuantityDto;
import com.monbat.planning.models.entities.BatteryQuantity;
import com.monbat.planning.repositories.BatteryQuantityRepository;
import com.monbat.planning.services.BatteryQuantityService;
import com.monbat.planning.services.utils.FindColumnIndexFromExcelRow;
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

import static com.monbat.planning.services.utils.columns.WarehouseColumns.*;

@Service
public class BatteryQuantityServiceImpl implements BatteryQuantityService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private BatteryQuantityRepository batteryQuantityRepository;

    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    @Override
    public void importBatteryQuantity(File file, String uploadId) {
        if (file != null) {
            this.batteryQuantityRepository.truncateTable();

            XSSFSheet sheet = ReadExcelFile.getWorksheet(file);

            Row headerRow = sheet.getRow(0);

            int materialColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, BATTERY_CODE);
            int quantityColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, QUANTITY);
            int productionPlantColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, PRODUCTION_PLANT);
            int storageLocationColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, STORAGE_LOCATION);
            int batchColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, BATCH);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                BatteryQuantityDto batteryQuantityDto = new BatteryQuantityDto();
                batteryQuantityDto.setBatteryCode(row.getCell(materialColumn).getStringCellValue());
                batteryQuantityDto.setQuantity((int) row.getCell(quantityColumn).getNumericCellValue());
                batteryQuantityDto.setProductionPlant(Integer.parseInt(row.getCell(productionPlantColumn).getStringCellValue()));
                batteryQuantityDto.setStorageLocation(Integer.parseInt(row.getCell(storageLocationColumn).getStringCellValue()));
                batteryQuantityDto.setBatch(row.getCell(batchColumn).getStringCellValue());

                batteryQuantityRepository.save(modelMapper.map(batteryQuantityDto, BatteryQuantity.class));
                progressMap.put(uploadId, (row.getRowNum() * 100) / (sheet.getLastRowNum() - 1));
            }
            progressMap.put(uploadId, 100);
        }
    }

    @Override
    public void startBatteryQuantity(File file, String uploadId) {
        importBatteryQuantity(file, uploadId);
    }

    @Override
    public List<BatteryQuantity> findAllBatteryQuantity() {
        return List.of();
    }

    @Override
    public List<BatteryQuantity> getAllByStorageLocation(int storageLocation) {
        return this.batteryQuantityRepository.findAllByStorageLocation(storageLocation);
    }

    @Override
    public List<BatteryQuantity> getAllByBatteryCodePrefix(int prefix) {
        return this.batteryQuantityRepository.findByBatteryCodeStartingWith(String.valueOf(prefix));
    }

    @Override
    public int getProgress(String uploadId) {
        return progressMap.getOrDefault(uploadId, 0);
    }
}
