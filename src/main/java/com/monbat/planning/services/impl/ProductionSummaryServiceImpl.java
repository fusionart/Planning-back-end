package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.ProductionSummaryDto;
import com.monbat.planning.models.entities.Material;
import com.monbat.planning.models.entities.ProductionSummary;
import com.monbat.planning.repositories.ProductionSummaryRepository;
import com.monbat.planning.services.MaterialService;
import com.monbat.planning.services.ProductionSummaryService;
import com.monbat.planning.services.utils.ConvertDateToLocalDate;
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
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.monbat.planning.services.utils.columns.ProductionSummaryColumns.*;
import static com.monbat.planning.utils.constants.Messages.SUCCESSFUL_IMPORT;

@Service
public class ProductionSummaryServiceImpl implements ProductionSummaryService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ProductionSummaryRepository productionSummaryRepository;
    @Autowired
    private MaterialService materialService;

    @Override
    public void importProductionSummary(ProductionSummaryRepository productionSummaryRepository,
                                        ModelMapper modelMapper, MultipartFile file) {

        if (file != null) {
            this.productionSummaryRepository.truncateTable();

            XSSFSheet sheet = ReadExcelFile.getWorksheet((File) file);

            Row headerRow = sheet.getRow(0);

            int scheduleStartDateColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, SCHEDULED_START_DATE);
            int workCenterColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, WORK_CENTER);
            int materialColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MATERIAL);
            int orderQuantityColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, ORDER_QUANTITY);
            int deliverQuantityColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, DELIVERED_QUANTITY);
            int systemStatusColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, SYSTEM_STATUS);
            int productionVersionsColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, PRODUCTION_VERSION);

            List<Material> materialList = this.materialService.getAllMaterialsByKilosForEachNot(1);


            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                if (row.getCell(materialColumn).getStringCellValue().isEmpty()) {
                    continue;
                }

                LocalDate date = ConvertDateToLocalDate.convertDateToLocalDate(row.getCell(scheduleStartDateColumn).getDateCellValue());

                ProductionSummaryDto productionSummaryDto = new ProductionSummaryDto();
                productionSummaryDto.setScheduleStartDate(date);
                productionSummaryDto.setWorkCenter(row.getCell(workCenterColumn).getStringCellValue());
                productionSummaryDto.setMaterial(row.getCell(materialColumn).getStringCellValue());
                productionSummaryDto.setTargetQuantity(row.getCell(orderQuantityColumn).getNumericCellValue());
                productionSummaryDto.setProductionVersion(row.getCell(productionVersionsColumn).getStringCellValue());

                Material material = materialList.stream().filter(item -> item.getMaterial().equals(row.getCell(materialColumn).getStringCellValue())).findFirst().orElse(null);

                if (material != null) {
                    productionSummaryDto.setDeliveredQuantity(row.getCell(deliverQuantityColumn).getNumericCellValue() * material.getNetWeight());
                } else {
                    productionSummaryDto.setDeliveredQuantity(row.getCell(deliverQuantityColumn).getNumericCellValue());
                }

                productionSummaryDto.setSystemStatus(row.getCell(systemStatusColumn).getStringCellValue());
                productionSummaryDto.setCalendarWeek(date.get(WeekFields.of(Locale.getDefault()).weekOfYear()) + "." + date.getYear());

                productionSummaryRepository.save(modelMapper.map(productionSummaryDto, ProductionSummary.class));
            }
        }
    }

    @Override
    public void startImportProductionSummary(MultipartFile file) {
        importProductionSummary(this.productionSummaryRepository, this.modelMapper, file);

        ShowAlert.showAlert(SUCCESSFUL_IMPORT);
    }

    @Override
    public Map<String, Map<String, Double>> sumWorkCenterQuantities() {
        List<ProductionSummary> productionSummaryList = this.productionSummaryRepository.findAll();

        return productionSummaryList.stream()
                .sorted()
                .collect(Collectors.groupingBy(
                        ProductionSummary::getCalendarWeek,
                        Collectors.groupingBy(ProductionSummary::getWorkCenter,
                                Collectors.summingDouble(ProductionSummary::getDeliveredQuantity)
                        )));
    }

    @Override
    public List<String> getDistinctReqDelDate() {
        List<ProductionSummary> productionSummaryList = this.productionSummaryRepository.findAll();

        return productionSummaryList.stream()
                .map(ProductionSummary::getCalendarWeek)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductionSummary> getAllProductionSummary() {
        return this.productionSummaryRepository.findAll();
    }
}
