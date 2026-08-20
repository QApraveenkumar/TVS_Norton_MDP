package utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;

public class ExcelReder {

  public Map<String, String> readSubwarrentydata(String filePath) throws IOException {
      Map<String, String> csvMap = new HashMap<>();
      InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
      if (is == null) throw new IOException("Resource not found: " + filePath);
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
          String line;
          boolean isHeader = true;
          int rowNumber = 0;
          while ((line = reader.readLine()) != null) {
              rowNumber++;
              if (isHeader) {
                  isHeader = false;
                  System.out.println("📄 CSV Header: " + line);
                  continue;
              }
              System.out.println("\n➡️ Reading CSV Row " + rowNumber + ": " + line);
              String[] csv = line.split(",", -1);
              if (csv.length < 4) {
                  System.out.println("⚠️ Invalid CSV row, skipping...");
                  continue;
              }
              String warrantyCode = csv[1].trim();
              String subWarrantyCode = csv[2].trim();
              String name = csv[3].trim();
              String key = warrantyCode + "-" + subWarrantyCode;
              System.out.println("🔑 CSV Key Built: " + key + " -> " + name);
              csvMap.put(key, name);
          }
      }
      System.out.println("✅ CSV Map Size: " + csvMap.size());
      return csvMap;
  }

  public Map<String, String> readWarrantyData(String filePath) throws IOException {
      Map<String, String> map = new HashMap<>();
      InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
      if (is == null) throw new IOException("Resource not found: " + filePath);
      try (Workbook workbook = WorkbookFactory.create(is)) {
          Sheet sheet = workbook.getSheetAt(0);
          boolean isHeader = true;
          int rowNumber = 0;
          for (Row row : sheet) {
              rowNumber++;
              if (isHeader) {
                  isHeader = false;
                  System.out.println("📄 Excel Header Row: " + rowNumber);
                  continue;
              }
              Cell countryCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
              Cell assemblyCodeCell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
              Cell assemblyNameCell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
              if (assemblyCodeCell == null || assemblyNameCell == null) {
                  System.out.println("⚠️ Invalid Excel row " + rowNumber + ", skipping...");
                  continue;
              }
              String country = countryCell != null ? countryCell.toString().trim() : "";
              String assemblyCode = assemblyCodeCell.toString().trim();
              String assemblyName = assemblyNameCell.toString().trim();
              String key = country + "-" + assemblyCode;
              System.out.println("🔑 Excel Key Built: " + key + " -> " + assemblyName);
              map.put(key, assemblyName);
          }
      } catch (Exception e) {
          throw new IOException("Failed to read workbook: " + e.getMessage(), e);
      }
      System.out.println("✅ Excel Map Size: " + map.size());
      return map;
  }
public Map<String, String> readFaultData(String filePath) throws IOException {
    Map<String, String> map = new HashMap<>();
    InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
    if (is == null) throw new IOException("Resource not found: " + filePath);
    try (Workbook workbook = WorkbookFactory.create(is)) {
        Sheet sheet = workbook.getSheetAt(0);
        boolean isHeader = true;
        int rowNumber = 0;
        for (Row row : sheet) {
            rowNumber++;
            if (isHeader) {
                isHeader = false;
                System.out.println("📄 Fault Excel Header Row: " + rowNumber);
                continue;
            }
            Cell countryCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Cell faultCodeCell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Cell faultNameCell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (faultCodeCell == null || faultNameCell == null) {
                System.out.println("⚠️ Invalid Fault Excel row " + rowNumber + ", skipping...");
                continue;
            }
            String country = countryCell != null ? countryCell.toString().trim() : "";
            String faultCode = faultCodeCell.toString().trim();
            String faultName = faultNameCell.toString().trim();
            String key = country + "-" + faultCode;
            System.out.println("🔑 Fault Excel Key Built: " + key + " -> " + faultName);
            map.put(key, faultName);
        }
    } catch (Exception e) {
        throw new IOException("Failed to read fault workbook: " + e.getMessage(), e);
    }
    System.out.println("✅ Fault Excel Map Size: " + map.size());
    return map;
}
}