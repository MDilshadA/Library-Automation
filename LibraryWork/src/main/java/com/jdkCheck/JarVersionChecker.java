package com.jdkCheck;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.*;

public class JarVersionChecker {
    private static final String MAVEN_API = "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&rows=1&wt=json";
    private static final String OUTPUT_EXCEL = "output/jar_versions.xlsx";
    private static final String DIRECTORY_PATH = "lib";
    private static final String SHEET_NAME = "Latest_Jar_Versions";

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(OUTPUT_EXCEL);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // ✅ Remove old sheet if it exists
            Sheet existingSheet = workbook.getSheet(SHEET_NAME);
            if (existingSheet != null) {
                workbook.removeSheetAt(workbook.getSheetIndex(existingSheet));
            }

            // ✅ Create a new sheet
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            createHeader(sheet);

            int rowIndex = 1;
            for (File jarFile : new File(DIRECTORY_PATH).listFiles()) {
                if (jarFile.getName().endsWith(".jar")) {
                    String jarName = jarFile.getName();
                    String[] extractedInfo = extractArtifactAndVersion(jarName);
                    if (extractedInfo == null) continue;

                    String artifactId = extractedInfo[0];
                    String currentVersion = extractedInfo[1];

                    String latestVersion = getLatestVersion(artifactId);
                    boolean updateAvailable = latestVersion != null && !latestVersion.equals(currentVersion);

                    writeToExcel(sheet, rowIndex++, jarName, currentVersion, latestVersion, updateAvailable);
                }
            }

            // ✅ Save the updated Excel file (without losing other sheets)
            try (FileOutputStream fos = new FileOutputStream(OUTPUT_EXCEL)) {
                workbook.write(fos);
            }
            System.out.println("Updated Excel file with new sheet: " + SHEET_NAME);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"JAR Name", "Current Version", "Latest Version", "Update Available"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(sheet.getWorkbook()));
        }
    }

    private static CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static String[] extractArtifactAndVersion(String jarName) {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9-]+)-([0-9]+(\\.[0-9]+)*).jar");
        Matcher matcher = pattern.matcher(jarName);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        return null;
    }

    private static String getLatestVersion(String artifactId) {
        try {
            String url = String.format(MAVEN_API, "*", artifactId);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray docs = json.getAsJsonObject("response").getAsJsonArray("docs");
            return (docs.size() > 0) ? docs.get(0).getAsJsonObject().get("v").getAsString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void writeToExcel(Sheet sheet, int rowIndex, String jarName, String currentVersion, String latestVersion, boolean updateAvailable) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(jarName);
        row.createCell(1).setCellValue(currentVersion);
        row.createCell(2).setCellValue(latestVersion != null ? latestVersion : "N/A");
        row.createCell(3).setCellValue(updateAvailable ? "YES" : "NO");
    }
}
