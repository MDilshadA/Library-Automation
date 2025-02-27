package org.library;

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

public class UpdateStatusChartGenerator {
//    private static final String EXCEL_PATH = "output/jar_versions.xlsx";

//    public static void main(String[] args) {
//        addPivotTableAndChart();
//    }

    public static void addPivotTableAndChart(String EXCEL_PATH) {
        try (FileInputStream fis = new FileInputStream(EXCEL_PATH);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet dataSheet = workbook.getSheet("jars_versions");
            if (dataSheet == null) {
                System.out.println("Sheet 'jars_versions' not found!");
                return;
            }

            int sheetIndex = workbook.getSheetIndex("UpdateStatus");
            if (sheetIndex != -1) {
                workbook.removeSheetAt(sheetIndex);
            }

            XSSFSheet pivotSheet = workbook.createSheet("UpdateStatus");

            int lastRow = dataSheet.getLastRowNum();
            System.out.println("Last Row : "+lastRow);
            if (lastRow < 1) {
                System.out.println("No data available to create a pivot table.");
                return;
            }


            String range = "A1:E" + (lastRow + 1);
            AreaReference source = new AreaReference(range, workbook.getSpreadsheetVersion());

            // Define where to place the pivot table
            CellReference position = new CellReference("A3");


            XSSFPivotTable pivotTable = pivotSheet.createPivotTable(source, position, dataSheet);


            pivotTable.addRowLabel(3); // column of source file here it is for Update Available


            pivotTable.addColumnLabel(DataConsolidateFunction.COUNT, 0, "Count");

            Row row = pivotSheet.createRow(1);
            row.createCell(0).setCellValue("Update Status");
            row.createCell(1).setCellValue("Count");

//            row = pivotSheet.createRow(2);
//            row.createCell(0).setCellValue("Up-to-date");
//            row.createCell(1).setCellFormula("COUNTIF(D:D, \"NO\")");
//
//            row = pivotSheet.createRow(3);
//            row.createCell(0).setCellValue("Outdated");
//            row.createCell(1).setCellFormula("COUNTIF(D:D, \"YES\")");


            XSSFDrawing drawing = pivotSheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 1, 18, 20);

            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText("Libraries / JAR's Status Overview");
            chart.setTitleOverlay(false);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.RIGHT);


            XDDFCategoryAxis xAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            xAxis.setTitle("Status Of Library");

            XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.LEFT);
            yAxis.setTitle("Count");


            XDDFDataSource<String> status1 = XDDFDataSourcesFactory.fromStringCellRange(pivotSheet,
                    new CellRangeAddress(2, 4, 0, 0));
            XDDFNumericalDataSource<Double> count1 = XDDFDataSourcesFactory.fromNumericCellRange(pivotSheet,
                    new CellRangeAddress(2, 4, 1, 1));


            XDDFChartData data = chart.createData(ChartTypes.BAR, xAxis, yAxis);
            XDDFChartData.Series series = data.addSeries(status1, count1);
            series.setTitle("Library Count", null);
            chart.plot(data);


            try (FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {
                workbook.write(fos);
                System.out.println("Pivot Table and Chart for Update_Status added successfully!");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
