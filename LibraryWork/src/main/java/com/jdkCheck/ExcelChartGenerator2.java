package com.jdkCheck;

import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelChartGenerator2 {
    private static final String EXCEL_PATH = "output/jar_versions.xlsx";

    public static void main(String[] args) {
        addPivotTableAndChart();
    }

    private static void addPivotTableAndChart() {
        try (FileInputStream fis = new FileInputStream(EXCEL_PATH);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet dataSheet = workbook.getSheet("jars_versions");
            if (dataSheet == null) {
                System.out.println("Sheet 'jars_versions' not found!");
                return;
            }

            int sheetIndex = workbook.getSheetIndex("PivotChart");
            if (sheetIndex != -1) {
                workbook.removeSheetAt(sheetIndex);
            }

            XSSFSheet pivotSheet = workbook.createSheet("PivotChart");

            int lastRow = dataSheet.getLastRowNum();
            System.out.println("Last Row : " + lastRow);
            if (lastRow < 1) {
                System.out.println("No data available to create a pivot table.");
                return;
            }

            String range = "A1:F" + (lastRow + 1);  // Assuming Column F contains JDK versions
            AreaReference source = new AreaReference(range, workbook.getSpreadsheetVersion());

            // Define where to place the pivot table
            CellReference position = new CellReference("A3");

            XSSFPivotTable pivotTable = pivotSheet.createPivotTable(source, position, dataSheet);

            // Set Row Label - "Update Available"
            pivotTable.addRowLabel(3);
            pivotTable.addColumnLabel(DataConsolidateFunction.COUNT, 0, "Count");

            // Add Update Status summary
            Row row = pivotSheet.createRow(1);
            row.createCell(0).setCellValue("Update Status");
            row.createCell(1).setCellValue("Count");

            row = pivotSheet.createRow(2);
            row.createCell(0).setCellValue("Up-to-date");
            row.createCell(1).setCellFormula("COUNTIF(D:D, \"NO\")");

            row = pivotSheet.createRow(3);
            row.createCell(0).setCellValue("Outdated");
            row.createCell(1).setCellFormula("COUNTIF(D:D, \"YES\")");

            // Create Bar Chart for Update Status
            createBarChart(pivotSheet, "Libraries/JAR's Status Overview", 4, 1, 12, 10, new CellRangeAddress(2, 3, 0, 0), new CellRangeAddress(2, 3, 1, 1));

            // Create JDK Support Summary
            Map<String, Integer> jdkCount = countJDKSupport(dataSheet, lastRow);

            int jdkStartRow = 6; // Start writing JDK data from row 6
            Row jdkHeaderRow = pivotSheet.createRow(jdkStartRow);
            jdkHeaderRow.createCell(0).setCellValue("JDK Version");
            jdkHeaderRow.createCell(1).setCellValue("Count");

            int rowIndex = jdkStartRow + 1;
            for (Map.Entry<String, Integer> entry : jdkCount.entrySet()) {
                Row jdkRow = pivotSheet.createRow(rowIndex++);
                jdkRow.createCell(0).setCellValue(entry.getKey());
                jdkRow.createCell(1).setCellValue(entry.getValue());
            }

            // Create Bar Chart for JDK Support
            createBarChart(pivotSheet, "JDK Support Overview", 4, 12, 12, 21, new CellRangeAddress(jdkStartRow + 1, rowIndex - 1, 0, 0), new CellRangeAddress(jdkStartRow + 1, rowIndex - 1, 1, 1));

            try (FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {
                workbook.write(fos);
                System.out.println("Pivot Table and Charts added successfully!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createBarChart(XSSFSheet sheet, String title, int col1, int row1, int col2, int row2, CellRangeAddress categoryRange, CellRangeAddress valueRange) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);

        XDDFCategoryAxis xAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        xAxis.setTitle("Category");

        XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.LEFT);
        yAxis.setTitle("Count");

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet, categoryRange);
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet, valueRange);

        XDDFChartData data = chart.createData(ChartTypes.BAR, xAxis, yAxis);
        XDDFChartData.Series series = data.addSeries(categories, values);
        series.setTitle(title, null);
        chart.plot(data);
    }

    private static Map<String, Integer> countJDKSupport(XSSFSheet dataSheet, int lastRow) {
        Map<String, Integer> jdkCount = new HashMap<>();

        for (int i = 1; i <= lastRow; i++) {
            Row row = dataSheet.getRow(i);
            if (row != null && row.getCell(5) != null) { // Assuming JDK Support is in column F
                String jdkSupport = row.getCell(5).getStringCellValue();
                if (jdkSupport != null && !jdkSupport.isEmpty()) {
                    String[] versions = jdkSupport.split(","); // Handling multiple versions per JAR
                    for (String version : versions) {
                        version = version.trim();
                        jdkCount.put(version, jdkCount.getOrDefault(version, 0) + 1);
                    }
                }
            }
        }
        return jdkCount;
    }
}

