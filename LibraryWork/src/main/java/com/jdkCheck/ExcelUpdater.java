package com.jdkCheck;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.*;

public class ExcelUpdater {
    private static final String EXCEL_FILE_PATH = "output/jar_versions.xlsx";

    public static void updateExcelWithNewSheet(Map<String, String[]> jarData, String sheetName) {
        try (FileInputStream fis = new FileInputStream(EXCEL_FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // ✅ Check if sheet already exists, remove if needed
            Sheet existingSheet = workbook.getSheet(sheetName);
            if (existingSheet != null) {
                workbook.removeSheetAt(workbook.getSheetIndex(existingSheet));
            }

            // ✅ Create a new sheet for latest JAR versions
            Sheet newSheet = workbook.createSheet(sheetName);

            // ✅ Create header row
            Row headerRow = newSheet.createRow(0);
            String[] headers = {"Jar_Name", "Current_Version", "Latest_Version", "Flag"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // ✅ Populate sheet with JAR data
            int rowIndex = 1;
            for (Map.Entry<String, String[]> entry : jarData.entrySet()) {
                Row row = newSheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(entry.getKey());  // Jar_Name
                row.createCell(1).setCellValue(entry.getValue()[0]); // Current_Version
                row.createCell(2).setCellValue(entry.getValue()[1]); // Latest_Version
                row.createCell(3).setCellValue(entry.getValue()[2]); // Flag (YES/NO)
            }

            // ✅ Save the workbook without losing other sheets
            try (FileOutputStream fos = new FileOutputStream(EXCEL_FILE_PATH)) {
                workbook.write(fos);
            }

            System.out.println("Updated Excel file with new sheet: " + sheetName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // ✅ Example JAR Data (Jar_Name -> {Current_Version, Latest_Version, Flag})
        Map<String, String[]> jarData = new HashMap<>();
        jarData.put("commons-io.jar", new String[]{"2.7", "2.13.0", "YES"});
        jarData.put("log4j-core.jar", new String[]{"2.17.1", "2.17.1", "NO"});
        jarData.put("jackson-core.jar", new String[]{"2.12.3", "2.15.0", "YES"});

        // ✅ Call method to update Excel
        updateExcelWithNewSheet(jarData, "Latest_Jar_Versions");
    }
}
