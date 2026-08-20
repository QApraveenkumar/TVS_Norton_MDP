package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;

import java.io.InputStream;
import java.util.List;

public class Dataprovider {

    @DataProvider(name = "createUsers")
    public static Object[][] createUsers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Load JSON from resources folder: src/test/resources/data/user_payloads.json
        try (InputStream is = Dataprovider.class.getResourceAsStream("/data/user_payloads.json")) {
            if (is == null) {
                throw new RuntimeException("File not found: /data/user_payloads.json");
            }

            // Read JSON array into a List of Map<String, Object>
            List<?> payloads = mapper.readValue(is, new TypeReference<List<?>>() {});

            // Convert List into Object[][] for TestNG
            Object[][] arr = new Object[payloads.size()][1];
            for (int i = 0; i < payloads.size(); i++) {
                arr[i][0] = payloads.get(i);
            }
            return arr;
        }
    }
}
