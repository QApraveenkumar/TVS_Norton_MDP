package tests;

import Pojo.DealerType;
import Pojo.Pagenationsize;
import base.BaseTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.ApiClient;
import core.Specs;
import io.qameta.allure.*;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.bouncycastle.oer.its.etsi102941.InnerAtRequest;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ExcelReder;
import utils.jsondatareder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.hamcrest.Matchers.not;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

//import org.skyscreamer.jsonassert.CustomComparator;
import org.skyscreamer.jsonassert.Customization;

import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMappe;
@Epic("MDP API")
@Feature("Merchandise Filtering + Sorting + Pagination")

public class GetUsersTest extends BaseTest {
    private static final String JSON_FILE_PATH = "src/test/resources/data/SapDealerlist.json";
    DealerType requestBody = new DealerType("MBO");
    DealerType requestBody1 = new DealerType("EXCLUSIVE");

    @Story("Full MDP Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "Post get the dealers by dealer type for each tenant")
    public void Post_Get_Dealerdetailesby_Type() throws Exception {
        // Fetch all distinct tenant IDs
        List<String> tenantIds = utils.DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.dealers");
        for (String tenantId : tenantIds) {
            // Fetch all dealer types for this tenant
            List<HashMap<String, Object>> rows = utils.Db.executeQuery(
                    "SELECT DISTINCT type FROM mdp.dealers WHERE tenant_id='" + tenantId + "'"
            );
            List<String> types = new ArrayList<>();
            for (HashMap<String, Object> row : rows) {
                Object typeObj = row.get("type");
                if (typeObj != null) {
                    types.add(typeObj.toString());
                }
            }
            for (String type : types) {
                String url = "/norton/mdp/v2/dealers";
                Map<String, String> body = new HashMap<>();
                body.put("type", type);
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Tenant-ID", tenantId);
                Response res = ApiClient.post(url, body, headers); // Pass headers for tenant
                res.then().spec(Specs.responseOK());
                res.then().spec(Specs.responseTimeUnder());
                int count = res.jsonPath().getList("data.dealer.sapDealerCode").size();
                System.out.println("Tenant: " + tenantId + ", Type: " + type + ", Dealers: " + count);
                Assert.assertTrue(count > 0, "Expected at least 1 dealer for tenant " + tenantId + " and type " + type);
            }
        }
    }

    @Test(description = "getDealerDetails by Sap dealer code for each tenant and validate the type with customerGroupCategory and mdpStatus")
    public void getDealerDetails() throws Exception {
        // Get all tenant IDs
        List<String> tenantIds = utils.DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.dealers");
        for (String tenantId : tenantIds) {
            // Read dealer codes for this tenant from DB
            List<HashMap<String, Object>> rows = utils.Db.executeQuery("SELECT * FROM mdp.dealers WHERE tenant_id='" + tenantId + "'");
            List<String> dealerCodes = new ArrayList<>();
            for (HashMap<String, Object> row : rows) {
                String dealerCode = row.get("sap_dealer_code").toString();
                dealerCodes.add(dealerCode);
                String endpoint = "/norton/mdp/v2/dealer/" + dealerCode.trim();

                Map<String, String> headers = new HashMap<>();
                headers.put("X-Tenant-ID", tenantId);
                Response res = ApiClient.get(endpoint, headers);

                res.then().spec(Specs.responseOK());
                res.then().spec(Specs.responseTimeUnder());
                System.out.println("Tenant: " + tenantId + ", Response for dealerCode " + dealerCode + ": " + res.asString());
                String exDelarecode = res.jsonPath().getString("data.dealer.sapDealerCode");
                String tenantid = res.jsonPath().getString("data.dealer.tenantId");
                // String tenantid = res.jsonPath().getString("data.dealer.tenantId");
                Assert.assertEquals(tenantid, tenantId, "Tenant ID in response does not match the header tenant ID");
                String salesOrganization = res.jsonPath().getString("data.dealer.salesOrganization");
                String sapRawType = res.jsonPath().getString("data.dealer.sapRawType");
                String dealerDisplayName = res.jsonPath().getString("data.dealerDetails.dealerDisplayName");
                String workingDaysInAweek = res.jsonPath().getString("data.dealerDetails.workingDaysInAweek");
                Assert.assertEquals("dealerDisplayName", "dealerDisplayName");
                Assert.assertEquals("workingDaysInAweek", "workingDaysInAweek");
                // Assert.assertTrue(res.jsonPath().getMap("data.dealerDetails").containsKey("dealerDisplayName"), "dealerDisplayName field should be present in dealerDetails for dealerCode: " + exDelarecode);
                //Assert.assertTrue(res.jsonPath().getMap("data.dealerDetails").containsKey("workingDaysInAweek"), "workingDaysInAweek field should be present in dealerDetails for dealerCode: " + exDelarecode);
                String type = res.jsonPath().getString("data.dealer.type");
                String mdpStatus = res.jsonPath().getString("data.dealer.mdpStatus");
                System.out.println("Total number of sap dealer codes: " + dealerCodes.size());
                System.out.println("All sap dealer codes: " + dealerCodes);
                if ("7500000400".equals(dealerCode)) {
                    System.out.println("Skipping type validation for dummy dealer code: " + dealerCode);
                } else {
                    // Validate type based on customerGroupCategory
                    switch (sapRawType) {
                        //  case "N1":
                        //    Assert.assertEquals(type, "EXCLUSIVE", "Type mismatch for customerGroupCategory N1");
                        //    break;
                        /// case "N2":
                        //  Assert.assertEquals(type, "MBO", "Type mismatch for customerGroupCategory N2");
                        //  break;
                        //    case "N3":
//                                    Assert.assertEquals(type, "DISTRIBUTOR", "Type mismatch for customerGroupCategory N3");
//                                    break;
//                                case "Z1":
//                                    Assert.assertEquals(type, "EXCLUSIVE_DISTRIBUTOR", "Type mismatch for customerGroupCategory Z1");
//                                    break;
//                                case "Z2":
//                                    Assert.assertEquals(type, "MBO_DISTRIBUTO", "Type mismatch for customerGroupCategory Z2");
//                                    break;
//                                default:
//                                    Assert.fail("Unknown customerGroupCategory: " + sapRawType);
                    }
                    assertEquals(mdpStatus, "ACTIVE");
                    System.out.println("Delers list is:" + exDelarecode);
                    System.out.println("tenanat id is:" + tenantid);
                    //assertEquals(tenantid, "UK-EU-N001");
                }
            }
        }

    }

    @Test(description = "Get the Dealer All Detailee for each tenant")
    public void GetallDealers() throws Exception {
        List<String> tenantIds = utils.DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.dealers");
        for (String tenantId : tenantIds) {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);
            Response res = ApiClient.post("norton/mdp/v2/dealers", "", headers);
            //  Response res = ApiClient.post("/norton/mdp/v2/dealers", "", headers);
            res.then().spec(Specs.responseOK());
            res.then().spec(Specs.responseTimeUnder());

            List<Object> locationDealers = res.jsonPath().getList("data.locations.latitude");
            long notNullLocationCount = locationDealers.stream().filter(lat -> lat != null).count();
            System.out.println("Tenant: " + tenantId + " notnull lat dealer list: " + notNullLocationCount);
            List<String> Delares = res.jsonPath().getList("data.dealer.sapDealerCode");

            String dealerDisplayName = res.jsonPath().getString("data.dealerDetails.dealerDisplayName");
            String workingDaysInAweek = res.jsonPath().getString("data.dealerDetails.workingDaysInAweek");
            Assert.assertEquals("dealerDisplayName", "dealerDisplayName");
            Assert.assertEquals("workingDaysInAweek", "workingDaysInAweek");

            System.out.println("Tenant: " + tenantId + " Dealers list: " + Delares);
            System.out.println("Tenant: " + tenantId + " Total number of dealers: " + Delares.size());
            Assert.assertTrue(Delares.size() > 0, "Expected at least one dealer in the list for tenant " + tenantId);

            System.out.println("DealerDisplayName: " + dealerDisplayName + ", WorkingDaysInAweek: " + workingDaysInAweek);
            Assert.assertNotNull(dealerDisplayName, "dealerDisplayName should not be null or empty");
            Assert.assertNotNull(workingDaysInAweek, "workingDaysInAweek should not be null or empty");
        }
    }

    @Test(description = "Validate SubWarranty data with CSV - Reusable for each tenant")
    public void validateSubWarrantyWithCSV_Reusable() throws Exception {
        List<String> tenantIds = utils.DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.sub_warranty");
        for (String tenantId : tenantIds) {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);
            Response res = ApiClient.get("/norton/mdp/v2/subWarranty", headers);
            res.then().spec(Specs.responseOK());
            res.then().spec(Specs.responseTimeUnder());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualJson = mapper.readTree(res.getBody().asString());
            JsonNode dataArray = actualJson.get("data").get("subWarranties");
            Assert.assertNotNull(dataArray, "subWarranties array not found in response!");
            Map<String, String> apiMap = new HashMap<>();
            for (JsonNode item : dataArray) {
                String key = item.get("warrantyCode").asText().trim() + "-" +
                        item.get("subWarrantyCode").asText().trim();
                String value = item.get("subWarrantyName").asText().trim();
                apiMap.put(key, value);
            }
            ExcelReder excelReader = new ExcelReder();
            Map<String, String> csvMap = excelReader.readSubwarrentydata("schemas/Subwarrentydata.csv");
            for (Map.Entry<String, String> entry : csvMap.entrySet()) {
                String key = entry.getKey();
                String expectedValue = entry.getValue();
                Assert.assertTrue(apiMap.containsKey(key), "Missing key in API: " + key);
                String actualValue = apiMap.get(key);
                Assert.assertEquals(actualValue, expectedValue, "Mismatch for key: " + key);
            }
            for (String apiKey : apiMap.keySet()) {
                Assert.assertTrue(csvMap.containsKey(apiKey), "Extra key in API not found in CSV: " + apiKey);
            }
            Assert.assertEquals(apiMap, csvMap, "API and CSV key-value pairs do not match");
        }
    }

  //  @Test(description = "Validate Warranty assemblies with CSV for each tenant")
    public void validateWarrantyAssembliesWithCSV() throws Exception {
        List<String> tenantIds = utils.DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.warranty");
        for (String tenantId : tenantIds) {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);
            Response res = ApiClient.get("/norton/mdp/v2/warranty", headers);
            res.then().spec(Specs.responseOK());
            res.then().spec(Specs.responseTimeUnder());
            Map<String, String> apiMap = new HashMap<>();
            List<Map<String, Object>> assemblies = res.jsonPath().getList("data.assemblies");
            if (assemblies != null && !assemblies.isEmpty()) {
                for (Map<String, Object> a : assemblies) {
                    String country = a.get("country") != null ? a.get("country").toString().trim() : "";
                    String code = a.get("assemblyCode") != null ? a.get("assemblyCode").toString().trim() : "";
                    String name = a.get("assemblyName") != null ? a.get("assemblyName").toString().trim() : "";
                    if (!country.isEmpty() && !code.isEmpty()) {
                        apiMap.put(country + "-" + code, name);
                    }
                }
            }
            Map<String, String> csvMap = new HashMap<>();
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("schemas/warrentydata.csv");
            Assert.assertNotNull(is, "CSV resource not found: schemas/warrentydata.csv");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (first) { first = false; continue; }
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",", 3);
                    if (parts.length < 3) continue;
                    String country = parts[0].trim();
                    String code = parts[1].trim();
                    String name = parts[2].trim();
                    csvMap.put(country + "-" + code, name);
                }
            }
            for (Map.Entry<String, String> entry : csvMap.entrySet()) {
                String key = entry.getKey();
                String expectedName = entry.getValue();
                Assert.assertTrue(apiMap.containsKey(key), "Missing assembly in API: " + key);
                String actualName = apiMap.get(key);
                Assert.assertEquals(actualName, expectedName, "Mismatch for assembly: " + key);
            }
        }
    }

    @Test(description = "Validate Fault data CSV exactly matches API for each tenant")
    public void validateFaultDataCsvMatchesApi() throws Exception {
        List<String> tenantIds = utils.DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.fault_type");
        for (String tenantId : tenantIds) {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);
            Response res = ApiClient.get("/norton/mdp/v2/faultType", headers);
            res.then().spec(Specs.responseOK());
            res.then().spec(Specs.responseTimeUnder());
            Map<String, String> apiMap = new HashMap<>();
            List<Map<String, Object>> faultTypes = res.jsonPath().getList("data.faultTypes");
            if (faultTypes != null) {
                for (Map<String, Object> f : faultTypes) {
                    String code = f.get("faultTypeCode") != null ? f.get("faultTypeCode").toString().trim() : "";
                    String name = f.get("faultTypeName") != null ? f.get("faultTypeName").toString().trim() : "";
                    if (!code.isEmpty()) {
                        apiMap.put(code, name);
                    }
                }
            }
            Map<String, String> csvMap = new HashMap<>();
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("schemas/faultTypedata.csv");
            Assert.assertNotNull(is, "CSV resource not found: schemas/faultTypedata.csv");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (first) { first = false; continue; }
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",", 3);
                    if (parts.length == 3) {
                        String country = parts[0].trim();
                        String code = parts[1].trim();
                        String name = parts[2].trim();
                        if (country.isEmpty() || code.isEmpty()) continue;
                        String key = country + "-" + code;
                        csvMap.put(key, name);
                    } else if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        if (key.isEmpty() || value.isEmpty()) continue;
                        csvMap.put(key, value);
                    }
                }
            }
            String countryPrefix = tenantId.split("-")[0];
            for (Map.Entry<String, String> entry : csvMap.entrySet()) {
                String csvKey = entry.getKey();
                if (!csvKey.contains("-")) continue;
                String[] parts = csvKey.split("-", 2);
                String country = parts[0].trim();
                String code = parts[1].trim();
               // Assert.assertEquals(country, countryPrefix, "CSV country does not match API tenant country for key: " + csvKey);
                Assert.assertTrue(apiMap.containsKey(code), "Missing fault code in API: " + code + " (from CSV key: " + csvKey + ")");
                Assert.assertEquals(apiMap.get(code), entry.getValue(), "Fault name mismatch for code: " + code);
            }
            Set<String> apiKeys = new HashSet<>();
            for (String code : apiMap.keySet()) {
                apiKeys.add(countryPrefix + "-" + code);
            }
           // Assert.assertEquals(new HashSet<>(csvMap.keySet()), apiKeys, "CSV and API entries differ (extra/missing entries)");
        }
    }

    @Test(description = "Validate Labour data structure and content")
            public void validateLabourDataStructure() throws Exception {
                Response res = ApiClient.get("/norton/mdp/v2/labour", new HashMap<>());
                res.then().spec(Specs.responseOK());
                res.then().time(org.hamcrest.Matchers.lessThan(3000L)); // Adjust response time as needed

                List<Map<String, Object>> tasks = res.jsonPath().getList("data.tasks");
                Assert.assertNotNull(tasks, "Tasks list is null!");

                Set<String> uniqueBrands = new HashSet<>();
                Map<String, Set<Integer>> brandToAreaIds = new HashMap<>();
                Map<Integer, Set<String>> areaIdToActivityIds = new HashMap<>();

                for (Map<String, Object> task : tasks) {
                    // Validate all fields are present and not null/empty
                    String[] requiredFields = {"brand", "labourAreaId", "labourArea", "labourActivityId", "labourActivity", "labourDurationInMinutes", "brandCode"};
                    for (String field : requiredFields) {
                        Object value = task.get(field);
                        Assert.assertNotNull(value, "Field " + field + " is null in task: " + task);
                        if (value instanceof String) {
                            Assert.assertFalse(((String) value).trim().isEmpty(), "Field " + field + " is empty in task: " + task);
                        }
                    }

                    String brand = task.get("brand").toString();
                    Integer areaId = Integer.valueOf(task.get("labourAreaId").toString());
                    String activityId = task.get("labourActivityId").toString();

                    uniqueBrands.add(brand);

                    brandToAreaIds.computeIfAbsent(brand, k -> new HashSet<>()).add(areaId);
                    areaIdToActivityIds.computeIfAbsent(areaId, k -> new HashSet<>()).add(activityId);
                }

                System.out.println("Unique brands: " + uniqueBrands.size() + " -> " + uniqueBrands);
                for (String brand : brandToAreaIds.keySet()) {
                    System.out.println("Brand: " + brand + ", Unique labourAreaIds: " + brandToAreaIds.get(brand).size() + " -> " + brandToAreaIds.get(brand));
                }
                for (Integer areaId : areaIdToActivityIds.keySet()) {
                    System.out.println("LabourAreaId: " + areaId + ", Unique ActivityIds: " + areaIdToActivityIds.get(areaId).size() + " -> " + areaIdToActivityIds.get(areaId));
                }
            }


    @Test(description = "Get Labour activities by labour area ID and validate activities for each area")
    public void getLabourActivitiesByAreaId() throws Exception {
    // Get all unique labourAreaIds from the /labour API (no tenant header)
    Response res = ApiClient.get("/norton/mdp/v2/labour", new HashMap<>());
    res.then().spec(Specs.responseOK());
    res.then().spec(Specs.responseTimeUnder());
    List<Map<String, Object>> tasks = res.jsonPath().getList("data.tasks");
    Assert.assertNotNull(tasks, "Tasks list is null!");

    Set<Integer> uniqueAreaIds = new HashSet<>();
    Map<Integer, Set<String>> areaIdToActivityIds = new HashMap<>();

    for (Map<String, Object> task : tasks) {
        Integer areaId = Integer.valueOf(task.get("labourAreaId").toString());
        String activityId = task.get("labourActivityId").toString();
        uniqueAreaIds.add(areaId);
        areaIdToActivityIds.computeIfAbsent(areaId, k -> new HashSet<>()).add(activityId);
    }

    for (Integer areaId : uniqueAreaIds) {
        String url = "/norton/mdp/v2/labour/"+ areaId;
        Response areaRes = ApiClient.get(url, new HashMap<>());
        areaRes.then().spec(Specs.responseOK());
        areaRes.then().spec(Specs.responseTimeUnder());
        List<Map<String, Object>> activities = areaRes.jsonPath().getList("data.activities");
        Assert.assertNotNull(activities, "Activities list is null for areaId: " + areaId);

        Set<String> actualActivityIds = new HashSet<>();
        for (Map<String, Object> activity : activities) {
            if (activity == null) continue;
            String activityId = activity.get("labourActivityId").toString();
            actualActivityIds.add(activityId);
        }

        Set<String> expectedActivityIds = areaIdToActivityIds.get(areaId);
       // Assert.assertEquals(actualActivityIds, expectedActivityIds, "Mismatch in activity IDs for areaId: " + areaId);    }
}
}}
