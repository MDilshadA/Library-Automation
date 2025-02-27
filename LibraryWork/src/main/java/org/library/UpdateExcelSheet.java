package org.library;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class UpdateExcelSheet {
    private static final String LIB_DIRECTORY = "lib";
    private static final String EXCEL_PATH = "output/jar_versions.xlsx";
    private static Map<String, String> nameVersionList;
    private static Map<String, String> latestVersion;
    private static Map<String, String> jdkSupport;

    public static void main(String[] args) {
        ensureExcelFileExists();
        nameVersionList = extractJarVersions(LIB_DIRECTORY);
        updateExcelWithJarData(nameVersionList);
        FlagStatus.flagActions(EXCEL_PATH);
        UpdateStatusChartGenerator.addPivotTableAndChart(EXCEL_PATH);
        FindJdkChartGenerator.addPivotTableAndChart(EXCEL_PATH);
    }

    private static void ensureExcelFileExists() {
        File excelFile = new File(EXCEL_PATH);
        if(excelFile.exists()){
            excelFile.delete();
        }

        if(!excelFile.exists() || excelFile.length() == 0) {
            createExcelFile();
        }else{
            System.err.println("There is something error while Creating Or deleting Excel File: "+EXCEL_PATH);
        }
    }

    private static void createExcelFile() {
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {
            Sheet sheet = workbook.createSheet("jars_versions");
            createHeaderRow(sheet, workbook);
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createHeaderRow(Sheet sheet, Workbook workbook) {
        Row headerRow = sheet.createRow(0);
        CellStyle style = createHeaderStyle(workbook);
        String[] headers = {"Library Name", "Current Version", "Latest Version", "Update Available", "JDK Compatibility"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, 40 * 256);
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static Map<String, String> extractJarVersions(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) return new HashMap<>();

        File[] files = getJarFilesRecursively(dir);

        Map<String, String> jarVersions = LibraryCurrentVersion.getCurrentVersions(files);
        latestVersion = LibraryLatestVersion.getLatestVersions(files);
        jdkSupport = JDKCompatibilityChecker.getJDKCompatibility(files);

        return jarVersions;
    }

    private static File[] getJarFilesRecursively(File directory) {
        List<File> jarFiles = new ArrayList<>();
        findJarFiles(directory, jarFiles);
        return jarFiles.toArray(new File[0]);
    }

    private static void findJarFiles(File directory, List<File> jarFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJarFiles(file, jarFiles);
                } else if (file.getName().toLowerCase().endsWith(".jar")) {
                    jarFiles.add(file);
                }
            }
        }
    }





    private static void updateExcelWithJarData(Map<String, String> jarVersions) {
        try (FileInputStream fis = new FileInputStream(EXCEL_PATH);
             Workbook workbook = new XSSFWorkbook(fis);
             FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 1;

            for (Map.Entry<String, String> entry : jarVersions.entrySet()) {
                String jarName = entry.getKey();
                String currentVersion = entry.getValue();
                String latest = latestVersion.getOrDefault(jarName, "NOT_FOUND");
                String updateAvailable = (!latest.equals("NOT_FOUND") && !latest.equals(currentVersion)) ? "YES" : "NO";
                String jdkCompatibility = jdkSupport.getOrDefault(jarName, "Unknown");

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(jarName);
                row.createCell(1).setCellValue(currentVersion);
                row.createCell(2).setCellValue(latest);
                row.createCell(3).setCellValue(updateAvailable);
                row.createCell(4).setCellValue(jdkCompatibility);
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
