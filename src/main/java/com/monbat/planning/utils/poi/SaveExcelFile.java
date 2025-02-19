package com.monbat.planning.utils.poi;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveExcelFile {
    public static void saveExcelToFile(XSSFWorkbook workbook, File file) {
        FileOutputStream out;

//        File directory = new File(fileAddress);
//        if (!directory.exists()) {
//            directory.mkdir();
//            // If you require it to make the entire directory path including parents,
//            // use directory.mkdirs(); here instead.
//        }

        try {
            out = new FileOutputStream(file);
            workbook.write(out);
            workbook.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
