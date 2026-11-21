// Reads test data from the classpath: CSV with OpenCSV, JSON with Jackson, Excel with Apache POI. Every reader returns rows as maps.
package framework.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderHeaderAware;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// --- keep
// One shape for all three sources (a list of column -> value maps), so a DataProvider does not care
// whether the rows came from CSV, JSON or a spreadsheet a tester filled in.
public final class DataFiles {

    private DataFiles() {
    }

    // --- keep
    // CSV: the first line is the header; CSVReaderHeaderAware gives each row as header -> value.
    public static List<Map<String, String>> csv(String path) {
        try (var reader = new CSVReaderHeaderAware(new InputStreamReader(open(path), StandardCharsets.UTF_8))) {
            List<Map<String, String>> rows = new ArrayList<>();
            for (Map<String, String> row = reader.readMap(); row != null; row = reader.readMap()) rows.add(row);
            return rows;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    // --- keep
    // JSON: Jackson reads an array of objects; nested objects (a whole request body) stay maps.
    public static List<Map<String, Object>> json(String path) {
        try (InputStream in = open(path)) {
            return new ObjectMapper().readValue(in, new TypeReference<>() { });
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    // --- keep
    // Excel: POI opens the workbook; row 0 is the header. DataFormatter returns each cell as the text
    // Excel shows, so a number typed as 2 is "2" and not 2.0.
    public static List<Map<String, String>> excel(String path, String sheetName) {
        try (InputStream in = open(path); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheet(sheetName);
            DataFormatter format = new DataFormatter();
            Row header = sheet.getRow(0);
            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                Map<String, String> values = new LinkedHashMap<>();
                for (int c = 0; c < header.getLastCellNum(); c++) {
                    values.put(format.formatCellValue(header.getCell(c)), format.formatCellValue(row.getCell(c)));
                }
                rows.add(values);
            }
            return rows;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    // --- keep
    // Files live in src/test/resources (on the classpath), so the same path works in the IDE and in CI.
    private static InputStream open(String path) {
        InputStream in = DataFiles.class.getResourceAsStream("/" + path);
        if (in == null) throw new IllegalStateException("No test data file " + path);
        return in;
    }
}
