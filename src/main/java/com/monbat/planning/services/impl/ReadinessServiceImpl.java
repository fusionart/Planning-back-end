package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.ReadinessDto;
import com.monbat.planning.models.entities.Readiness;
import com.monbat.planning.repositories.ReadinessRepository;
import com.monbat.planning.services.ReadinessService;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.monbat.planning.services.utils.columns.ReadinessColumns.*;
import static com.monbat.planning.utils.constants.Messages.SUCCESSFUL_IMPORT;

@Service
public class ReadinessServiceImpl implements ReadinessService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ReadinessRepository readinessRepository;

    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    @Override
    public List<Readiness> findAllReadiness() {
        return null;
    }

    @Override
    public List<String> getDistinctReqDelDate() {
        List<Readiness> allReadiness = this.readinessRepository.findAll();

        return allReadiness.stream()
                .map(Readiness::getReqDlvWeek)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getDistinctReadinessWeek() {
        List<Readiness> allReadiness = this.readinessRepository.findAll();

        return allReadiness.stream()
                .map(Readiness::getWeekOfReadiness)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<Readiness> getAllByByWeekOfReadiness(String weekOfReadiness, int productionPlan) {
        return this.readinessRepository.findAllByWeekOfReadinessAndProductionPlant(weekOfReadiness, productionPlan);
    }

    @Override
    public Map<String, Integer> sumMaterialQuantitiesByReqDlvWeek(String reqDlvWeek, int productionPlant) {
        List<Readiness> readinessList = this.readinessRepository.findAllByReqDlvWeekAndProductionPlant(reqDlvWeek, productionPlant);

        return readinessList.stream()
                .sorted()
                .collect(Collectors.groupingBy(
                        Readiness::getMaterial,
                        Collectors.summingInt(Readiness::getOrderQuantity)
                ));
    }

    @Override
    public void importReadiness(File file, String uploadId) {
        if (file != null) {
            this.readinessRepository.truncateTable();

            XSSFSheet sheet = ReadExcelFile.getWorksheet(file);

            Row headerRow = sheet.getRow(0);

            int salesDocumentColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, SALES_DOCUMENT);
            int soldToPartyColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, SOLD_TO_PARTY);
            int customerNameColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, CUSTOMER_NAME);
            int dateOfReadinessColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, DATE_OF_READINESS);
            int weekOfReadinessColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, WEEK_OF_READINESS);
            int reqDlvWeekColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, REQ_DLV_WEEK);
            int materialColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MATERIAL);
            int orderQuantityColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, ORDER_QUANTITY);
            int productionPlantColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, PRODUCTION_PLAN);


            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                if (row.getCell(materialColumn).getStringCellValue().startsWith("10")) {
                    ReadinessDto readinessDto = new ReadinessDto();
                    readinessDto.setSalesDocument(Integer.parseInt(row.getCell(salesDocumentColumn).getStringCellValue()));
                    readinessDto.setSoldToParty(row.getCell(soldToPartyColumn).getStringCellValue());
                    readinessDto.setCustomerName(row.getCell(customerNameColumn).getStringCellValue());
                    readinessDto.setDateOfReadiness(row.getCell(dateOfReadinessColumn).getDateCellValue());
                    readinessDto.setWeekOfReadiness(row.getCell(weekOfReadinessColumn).getStringCellValue());

                    //check if the value is week only or not
                    if (row.getCell(reqDlvWeekColumn).getStringCellValue().length() <= 7) {
                        readinessDto.setReqDlvWeek(row.getCell(reqDlvWeekColumn).getStringCellValue());
                    } else {
                        //if value is full date, get only the week and year
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                        LocalDate date = LocalDate.parse(row.getCell(reqDlvWeekColumn).getStringCellValue(), formatter);
                        WeekFields weekFields = WeekFields.of(Locale.getDefault());

                        readinessDto.setReqDlvWeek(date.get(weekFields.weekOfWeekBasedYear()) + "." + date.getYear());
                    }

                    readinessDto.setMaterial(row.getCell(materialColumn).getStringCellValue());
                    readinessDto.setOrderQuantity((int) row.getCell(orderQuantityColumn).getNumericCellValue());
                    readinessDto.setProductionPlant(Integer.parseInt(row.getCell(productionPlantColumn).getStringCellValue()));

                    if (row.getCell(materialColumn).getStringCellValue().endsWith("L0") || row.getCell(materialColumn).getStringCellValue().endsWith("R0")) {
                        readinessDto.setBatteryType("SLI Dry");
                    }

                    if (row.getCell(materialColumn).getStringCellValue().endsWith("L1") || row.getCell(materialColumn).getStringCellValue().endsWith("R1")) {
                        readinessDto.setBatteryType("SLI Wet");
                    }

                    if (row.getCell(materialColumn).getStringCellValue().endsWith("O") || row.getCell(materialColumn).getStringCellValue().endsWith("L") || row.getCell(materialColumn).getStringCellValue().endsWith("F")) {
                        readinessDto.setBatteryType("RP/VRLA");
                    }

                    readinessRepository.save(modelMapper.map(readinessDto, Readiness.class));

                    progressMap.put(uploadId, (row.getRowNum() * 100) / (sheet.getLastRowNum() - 1));
                }
            }
            progressMap.put(uploadId, 100);
        }
    }

    @Override
    public void startImportReadiness(File file, String uploadId) {
        importReadiness(file, uploadId);
    }

    @Override
    public int getProgress(String uploadId) {
        return progressMap.getOrDefault(uploadId, 0);
    }

}
