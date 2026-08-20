package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class jsondatareder {
private static ObjectMapper mapper = new ObjectMapper();
    // This method explicitly reads 'SapDealerIds' key from JSON
    public static List<String> getSapDealerIds(String jsonFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(jsonFilePath)) {
            JsonNode rootNode = mapper.readTree(fis);
            JsonNode dealerCodesNode = rootNode.path("SapDealerIds");
            List<String> dealerCodes = new ArrayList<>();
            dealerCodesNode.forEach(node -> dealerCodes.add(node.asText()));
            return dealerCodes;
        }

    }}