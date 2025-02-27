package org.library;

import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FlagStatus {
    public static void flagActions(String filePath) {
        try {

            FileInputStream fis = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            fis.close();


            XSSFSheet sheet = workbook.getSheet("jars_versions");
//            int sheetIndex = workbook.getSheetIndex(sheet);
//            System.out.println("Sheet Index = : "+sheetIndex);
//            if (sheet == null) {
//                sheet = workbook.createSheet("sheet1");
//            }

            // range (D2:D20)
            int startRow = 1, endRow = 40, column = 3; // Column D = index 3 (0-based)


            SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
            ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule("D2=\"YES\"");


            FontFormatting fontFmt = rule.createFontFormatting();
            fontFmt.setFontColorIndex(IndexedColors.RED.getIndex());
            fontFmt.setFontStyle(false, true);

            // Apply rule to range D2:D20
            CellRangeAddress[] range = { new CellRangeAddress(startRow, endRow, column, column) };
            sheetCF.addConditionalFormatting(range, rule);


            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
            workbook.close();

            System.out.println("Conditional formatting applied successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
