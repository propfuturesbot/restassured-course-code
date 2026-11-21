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

    // ---
    // CSV: the first line is the header; CSVReaderHeaderAware gives each row as header -> value.


    // ---
    // JSON: Jackson reads an array of objects; nested objects (a whole request body) stay maps.


    // ---
    // Excel: POI opens the workbook; row 0 is the header. DataFormatter returns each cell as the text
    // Excel shows, so a number typed as 2 is "2" and not 2.0.


    // --- keep
    // Files live in src/test/resources (on the classpath), so the same path works in the IDE and in CI.
    private static InputStream open(String path) {
        InputStream in = DataFiles.class.getResourceAsStream("/" + path);
        if (in == null) throw new IllegalStateException("No test data file " + path);
        return in;
    }
}
