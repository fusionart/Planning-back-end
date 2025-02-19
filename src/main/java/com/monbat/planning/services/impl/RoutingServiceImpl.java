package com.monbat.planning.services.impl;

import com.monbat.planning.models.dto.RoutingDto;
import com.monbat.planning.models.entities.ActivityType;
import com.monbat.planning.models.entities.Routing;
import com.monbat.planning.repositories.RoutingRepository;
import com.monbat.planning.services.RoutingService;
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

import static com.monbat.planning.services.utils.columns.RoutingFileColumnNames.*;
import static com.monbat.planning.utils.constants.Messages.SUCCESSFUL_IMPORT;

@Service
public class RoutingServiceImpl implements RoutingService {
    @Autowired
    private RoutingRepository routingRepository;
    @Autowired
    private ModelMapper modelMapper;

    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    @Override
    public boolean areImported() {
        return this.routingRepository.count() > 0;
    }

    @Override
    public void importRoutings(File file, String uploadId) {
        if (file != null) {
            XSSFSheet sheet = ReadExcelFile.getWorksheet(file);
            //Clear the table before new import
            routingRepository.truncateTable();

            Row headerRow = sheet.getRow(0);

            //get all columns numbers
            int routingGroupColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, ROUTING_GROUP_COLUMN);
            int routingGroupCounterColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, ROUTING_GROUP_COUNTER_COLUMN);
            int plantColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, PLAN_COLUMN);
            int workCenterColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, WORK_CENTER_COLUMN);
            int baseQuantityColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, BASE_QUANTITY_COLUMN);

            //SH1
            int setupMachineActivityTypeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, SETUP_TIME_ACTIVITY_TYPE_COLUMN);
            int setupMachineUomColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, SETUP_TIME_UOM_COLUMN);
            int setupMachineTimeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, SETUP_TIME_VALUE_COLUMN);
            //MH1
            int machineActivityTypeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MACHINE_TIME_ACTIVITY_TYPE_COLUMN);
            int machineUomColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MACHINE_TIME_UOM_COLUMN);
            int machineTimeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, MACHINE_TIME_VALUE_COLUMN);
            //PH1
            int laborActivityTypeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, LABOR_TIME_ACTIVITY_TYPE_COLUMN);
            int laborUomColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, LABOR_TIME_UOM_COLUMN);
            int laborTimeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, LABOR_TIME_VALUE_COLUMN);
            //GS1
            int naturalGasActivityTypeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, NATURAL_GAS_ACTIVITY_TYPE_COLUMN);
            int naturalGasUomColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, NATURAL_GAS_UOM_COLUMN);
            int naturalGasTimeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, NATURAL_GAS_VALUE_COLUMN);
            //EH1
            int electricityActivityTypeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, ELECTRICITY_ACTIVITY_TYPE_COLUMN);
            int electricityUomColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, ELECTRICITY_UOM_COLUMN);
            int electricityTimeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, ELECTRICITY_VALUE_COLUMN);
            //H2O
            int waterActivityTypeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, WATER_ACTIVITY_TYPE_COLUMN);
            int waterUomColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, WATER_UOM_COLUMN);
            int waterTimeColumn = FindColumnIndexFromExcelRow.findColumnIndex(headerRow, WATER_VALUE_COLUMN);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                Routing routing = getRoutingGroupAndRoutingGroupCounter(row.getCell(routingGroupColumn).getStringCellValue(), Integer.parseInt(row.getCell(routingGroupCounterColumn).getStringCellValue()));

                if (routing == null) {
                    RoutingDto routingDto = new RoutingDto();

                    routingDto.setRoutingGroupCounter(Integer.parseInt(row.getCell(routingGroupCounterColumn).getStringCellValue()));
                    routingDto.setRoutingGroup(row.getCell(routingGroupColumn).getStringCellValue());
                    routingDto.setPlant(Integer.parseInt(row.getCell(plantColumn).getStringCellValue()));
                    routingDto.setWorkCenter(row.getCell(workCenterColumn).getStringCellValue());
                    routingDto.setDescription("");
                    routingDto.setBaseQuantity((int) row.getCell(baseQuantityColumn).getNumericCellValue());

                    routingDto.setSetupTime(createActivityType(setupMachineActivityTypeColumn, setupMachineUomColumn, setupMachineTimeColumn, row));
                    routingDto.setMachineTime(createActivityType(machineActivityTypeColumn, machineUomColumn, machineTimeColumn, row));
                    routingDto.setLaborTime(createActivityType(laborActivityTypeColumn, laborUomColumn, laborTimeColumn, row));
                    routingDto.setNaturalGas(createActivityType(naturalGasActivityTypeColumn, naturalGasUomColumn, naturalGasTimeColumn, row));
                    routingDto.setElectricity(createActivityType(electricityActivityTypeColumn, electricityUomColumn, electricityTimeColumn, row));
                    routingDto.setWater(createActivityType(waterActivityTypeColumn, waterUomColumn, waterTimeColumn, row));

                    routingRepository.save(modelMapper.map(routingDto, Routing.class));
                    progressMap.put(uploadId, (row.getRowNum() * 100) / (sheet.getLastRowNum() - 1));
                }
            }
            progressMap.put(uploadId, 100);
        }
    }

    @Override
    public List<Routing> getAllRoutings() {
        return this.routingRepository.findAll().stream().toList();
    }

    @Override
    public Routing getRoutingGroupAndRoutingGroupCounter(String routingGroup, int routingGroupCounter) {
        return this.routingRepository.findFirstByRoutingGroupAndRoutingGroupCounter(routingGroup, routingGroupCounter);
    }

    private ActivityType createActivityType(int activityTypeColumn, int uomColumn, int valueColumn, Row row) {
        ActivityType activityType = new ActivityType();

        activityType.setType(row.getCell(activityTypeColumn).getStringCellValue());
        activityType.setUom(row.getCell(uomColumn).getStringCellValue());
        activityType.setValue(row.getCell(valueColumn).getNumericCellValue());

        return activityType;
    }

    @Override
    public void startImportRouting(File file, String uploadId) {
        importRoutings(file, uploadId);

    }

    @Override
    public int getProgress(String uploadId) {
        return progressMap.getOrDefault(uploadId, 0);

    }
}