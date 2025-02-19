package com.monbat.planning.services.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class FindColumnIndexFromExcelRow {
    public static int findColumnIndex(Row row, String searchValue) {
        for (Cell cell : row) {
            if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().equals(searchValue)) {
                return cell.getColumnIndex();
            }
        }
        System.out.println(searchValue + " not found");
        return -1;
    }
}
