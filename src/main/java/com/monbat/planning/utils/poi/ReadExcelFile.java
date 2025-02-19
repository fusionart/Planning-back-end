package com.monbat.planning.utils.poi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReadExcelFile {
    public static XSSFSheet getWorksheet(File file) {
        XSSFSheet sheet;
        try (InputStream inp = new FileInputStream(file)) {
            XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(inp);
            sheet = wb.getSheetAt(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sheet;
    }
}
