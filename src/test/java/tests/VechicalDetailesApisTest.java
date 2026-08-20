package tests;

import Pojo.Pagenationsize;
import base.BaseTest;
import core.ApiClient;
import core.Specs;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;
import utils.DBUtils;
import utils.Db;
import utils.DBUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.sql.DriverManager.println;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VechicalDetailesApisTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(VechicalDetailesApisTest.class);
    private static final String JSON_FILE_PATH = "src/test/resources/data/partids.json";
    private static final String BASE_API_URL = "/norton/mdp/v2";

    private Response fetchApiResponse(String endpoint, Map<String, String> headers) {
        try {
            Response response = ApiClient.get(BASE_API_URL + endpoint, headers);
            response.then().spec(Specs.responseTimeUnder()).spec(Specs.responseOK());
            return response;
        } catch (Exception e) {
            logger.error("Error fetching API response for endpoint: {}", endpoint, e);
            throw e;
        }
    }

    private void validateResponseField(Object field, String fieldName, String errorMessage) {
        Assert.assertNotNull(field, errorMessage + " Field: " + fieldName);
    }

    @Test
    public void getallVechicalsapmodelids() throws Exception {
        List<String> tenantIds = DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.vehicle_models");
        for (String tenantId : tenantIds) {
            logger.info("Starting test: getallVechicalsapmodelids for tenant: {}", tenantId);
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);
            Response res = fetchApiResponse("/product/vehicle/sap-model-ids", headers);
            List<Object> rawSapModelIDs = res.jsonPath().getList("data");
            logger.info("Tenant: {} | Raw sapModelIDs: {}", tenantId, rawSapModelIDs);
            logger.info("Tenant: {} | Total sapModelIDs: {}", tenantId, (rawSapModelIDs != null ? rawSapModelIDs.size() : 0));
            System.out.println("Tenant: " + tenantId + " | Total count of sapModelIDs: " + (rawSapModelIDs != null ? rawSapModelIDs.size() : 0));

            if (rawSapModelIDs != null) {
                Set<Object> uniqueModelIds = new HashSet<>(rawSapModelIDs);
                if (uniqueModelIds.size() != rawSapModelIDs.size()) {
                    logger.warn("Tenant: {} | Duplicate model IDs found! Total: {}, Unique: {}", tenantId, rawSapModelIDs.size(), uniqueModelIds.size());
                } else {
                    logger.info("Tenant: {} | No duplicate model IDs found.", tenantId);
                }
            }
        }
    }

    @Test
    public void getVehicleModelIdsWithParts() throws Exception {
        List<String> tenantIds = DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.vehicle_models");
        for (String tenantId : tenantIds) {
            logger.info("Starting test: getVehicleModelIdsWithParts for tenant: {}", tenantId);
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);

            Response res1 = fetchApiResponse("/product/vehicle/sap-model-ids?tenant_id=" + tenantId, headers);
            List<String> sapModelIds = res1.jsonPath().getList("data", String.class);

            Set<String> globalPartIds = new HashSet<>();
            Map<String, Set<String>> partIdToModels = new LinkedHashMap<>();

            for (String sapModelId : sapModelIds) {
                Response res = fetchApiResponse("/product/vehicle/" + sapModelId + "?partsData=true&tenant_id=" + tenantId, headers);
                List<Map<String, Object>> vehicleParts = res.jsonPath().getList("data.vehicleParts");
                if (vehicleParts == null) vehicleParts = Collections.emptyList();

                Set<String> partsSeenInThisModel = new HashSet<>();
                List<String> sapPartslIDs = new ArrayList<>();

                for (Map<String, Object> part : vehicleParts) {
                    String sapPartId = (String) part.get("sapPartId");
                    validateResponseField(sapPartId, "sapPartId", "Blank sapPartId found in model: " + sapModelId);

                    Assert.assertTrue(partsSeenInThisModel.add(sapPartId),
                            "Duplicate sapPartId within the same model: " + sapPartId + " (model: " + sapModelId + ")");

                    sapPartslIDs.add(sapPartId);

                    partIdToModels.computeIfAbsent(sapPartId, k -> new LinkedHashSet<>()).add(sapModelId);
                    if (!globalPartIds.add(sapPartId)) {
                        logger.warn("Tenant: {} | Duplicate sapPartId detected across models: {}", tenantId, sapPartId);
                    }
                }

                logger.info("Tenant: {} | For sapModelID: {} Total sapPartslIDs: {}, sapPartslIDs: {}", tenantId, sapModelId, sapPartslIDs.size(), sapPartslIDs);
            }

            Map<String, Set<String>> crossModelDuplicates = new LinkedHashMap<>();
            for (Map.Entry<String, Set<String>> e : partIdToModels.entrySet()) {
                if (e.getValue().size() > 1) {
                    crossModelDuplicates.put(e.getKey(), e.getValue());
                }
            }

            if (!crossModelDuplicates.isEmpty()) {
                logger.error("Tenant: {} | Duplicate part IDs found across models: {}", tenantId, crossModelDuplicates.keySet());
                Assert.fail("Tenant: " + tenantId + " | Duplicate part IDs across models: " + crossModelDuplicates.keySet());
            } else {
                logger.info("Tenant: {} | No duplicate part IDs found across models.", tenantId);
            }
        }
    }

    @Test(description = "Get parts details by part id and validate part name and partDescription")
    public void Getpartsdetailesbypartid() throws Exception {
        List<String> tenantIds = DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.vehicle_models");
        for (String tenantId : tenantIds) {
            logger.info("Starting test: Getpartsdetailesbypartid for tenant: {}", tenantId);
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);

            Response res1 = fetchApiResponse("/product/vehicle/sap-model-ids?tenant_id=" + tenantId, headers);
            List<Object> sapModelIDs = res1.jsonPath().getList("data");

            for (Object sapModelIdObj : sapModelIDs) {
                String sapModelId = sapModelIdObj.toString();
                Response res2 = fetchApiResponse("/product/vehicle/" + sapModelId + "?partsData=true&tenant_id=" + tenantId, headers);
                List<Map<String, Object>> vehicleParts = res2.jsonPath().getList("data.vehicleParts");

                int totalParts = vehicleParts != null ? vehicleParts.size() : 0;
                logger.info("Tenant: {} | Total parts for model {}: {}", tenantId, sapModelId, totalParts);

                if (vehicleParts != null) {
                    for (Map<String, Object> part : vehicleParts) {
                        String sapPartId = part.get("sapPartId").toString();
                        String partName = part.get("partDescription").toString();

                        Response partRes = ApiClient.get(BASE_API_URL + "/product/vehicle/part/" + sapPartId, headers);
                        partRes.then().spec(Specs.responseTimeUnder());
                        partRes.then().spec(Specs.responseOK());

                        String respPartDes = partRes.jsonPath().getString("data.vehiclePart.partDescription");
                        String respPartName = partRes.jsonPath().getString("data.vehiclePart.sapPartId");
                        String respModelId = partRes.jsonPath().getString("data.vehicleModel.sapModelId");
                        String availabilityStatus = partRes.jsonPath().getString("data.vehicleModel.availabilityStatus");

                        logger.info("Tenant: {} | responseModelId: {}, responsePartId: {}, responsePartName: {}, availabilityStatus: {}", tenantId, respModelId, sapPartId, respPartName, availabilityStatus);

                        Assert.assertEquals(sapPartId, respPartName, "Part name mismatch for partId: " + sapPartId);
                        Assert.assertEquals(partName, respPartDes, "Part description mismatch for partName: " + partName);
                        Assert.assertEquals(sapModelId, respModelId, "Model ID mismatch for partId: " + sapPartId);
                        Assert.assertEquals("ACTIVE", availabilityStatus, "availabilityStatus should be ACTIVE");

                        String vehicleVariant = partRes.jsonPath().getString("data.vehiclePart.variant");
                        String vehicleColor = partRes.jsonPath().getString("data.vehiclePart.color");
                        logger.info("Tenant: {} | vehicleVariant: {}, vehicleColor: {}", tenantId, vehicleVariant, vehicleColor);
                    }
                }
            }
        }
    }

    //@Test(description = "Get price details for part IDs from DB")
@Test(description = "Get vehicle price details for ALL tenants (multi-tenant by location)")
public void getPartsPriceDetails_MultiTenant() throws Exception {
    // 1️⃣ Fetch all tenant/location/currency/state mappings
    List<HashMap<String, Object>> locationRows = Db.executeQuery(
            "SELECT id, country, currency, state, tenant_id FROM mdp.price_location_details"
    );

    for (HashMap<String, Object> locationRow : locationRows) {
        String tenantId = String.valueOf(locationRow.get("tenant_id"));
        String locationId = String.valueOf(locationRow.get("id"));
        String country = String.valueOf(locationRow.get("country"));
        String currency = String.valueOf(locationRow.get("currency"));
        String state = String.valueOf(locationRow.get("state"));

        logger.info("Processing Tenant={} Location={} Country={} State={}", tenantId, locationId, country, state);

        // 2️⃣ Fetch all part IDs for this location
        List<HashMap<String, Object>> partRows = Db.executeQuery(
                "SELECT part_id FROM mdp.price_vehicle_model WHERE location_id = '" + locationId + "'"
        );

        for (HashMap<String, Object> partRow : partRows) {
            String sapPartId = String.valueOf(partRow.get("part_id"));

            logger.info("Executing for Tenant={} Location={} Part={}", tenantId, locationId, sapPartId);

            // 3️⃣ Header (dynamic tenant)
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);

            // 4️⃣ API call
            String requestUrl = BASE_API_URL + "/price/vehicle/" + sapPartId + "?state=" + state + "&country=" + country;

            Response priceRes = ApiClient.get(requestUrl, headers);

            priceRes.then().spec(Specs.responseOK());

            // 5️⃣ Response validation
            String respTenantCountry = priceRes.jsonPath().getString("data.vehicleExShowroomPrice.country");
            String respCurrency = priceRes.jsonPath().getString("data.vehicleExShowroomPrice.currency");
            String price = priceRes.jsonPath().getString("data.vehicleExShowroomPrice.price");

           // Assert.assertNotNull(price, "Price should not be null");
            Assert.assertNotNull(respCurrency, "Currency should not be null");
            Assert.assertEquals(respTenantCountry, country, "Country mismatch for partId: " + sapPartId);
            Assert.assertEquals(respCurrency, currency, "Currency mismatch for partId: " + sapPartId);

            logger.info(
                    "✅ Tenant={} Location={} Part={} Price={} Currency={}",
                    tenantId, locationId, sapPartId, price, respCurrency
            );
        }
    }
}

    @Test(description = "Validate merchandise details from DB and API response")
    public void validateMerchandisePriceDetails() throws Exception {
                List<String> tenantIds = DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.price_location_details");
                for (String tenantId : tenantIds) {
                    logger.info("Starting test: validateMerchandisePriceDetails for tenant: {}", tenantId);
                    Map<String, String> headers = new HashMap<>();
                    headers.put("X-Tenant-ID", tenantId);

                    List<String> partIds = new ArrayList<>();
                    List<HashMap<String, Object>> rows = Db.executeQuery("select * from mdp.product_price where location_id ='1'");
                    for (HashMap<String, Object> row : rows) {
                        Object partIdObj = row.get("part_id");
                        if (partIdObj != null) {
                            partIds.add(partIdObj.toString());
                        }
                    }

                    int priceFoundCount = 0;
                    int priceNotFoundCount = 0;

                    for (String partNumber : partIds) {
                        String requestUrl = BASE_API_URL + "/price/merchandise/" + partNumber;
                        Response res = ApiClient.get(requestUrl, headers);

                        // If data is null or errorMessage contains "No merchandise record found", skip this part
                        Object data = res.jsonPath().get("data");
                        String errorMessage = res.jsonPath().getString("errorMessage");
                        if (data == null || (errorMessage != null && errorMessage.contains("No merchandise record found"))) {
                            logger.warn("Tenant: {} | No price data for partId: {}", tenantId, partNumber);
                            priceNotFoundCount++;
                            continue;
                        }

                        res.then().spec(Specs.responseTimeUnder());
                        res.then().spec(Specs.responseOK());
                        String respPartId = res.jsonPath().getString("data.merchandisePrice.sapPartId");
                        Set<String> seenPartIds = new HashSet<>();
                        Assert.assertTrue(seenPartIds.add(respPartId), "Duplicate sapPartId found: " + respPartId);
                        String currency = res.jsonPath().getString("data.merchandisePrice.currency");
                        String country = res.jsonPath().getString("data.merchandisePrice.country");
                        String price = res.jsonPath().getString("data.merchandisePrice.price");
                        Map<String, Object> exShowroomPrice = res.jsonPath().getMap("data.vehicleExShowroomPrice");
                        if (exShowroomPrice != null) {
                            Assert.assertTrue(exShowroomPrice.containsKey("sapPartId"), "'sapPartId' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("dealerPrice"), "'dealerPrice' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("dealerStdDiscPercent"), "'dealerStdDiscPercent' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("dealerAddDiscPercent"), "'dealerAddDiscPercent' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("dealerFreightCharges"), "'dealerFreightCharges' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("dealerVatPercent"), "'dealerVatPercent' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("retailPrice"), "'retailPrice' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("retailStdDiscPercent"), "'retailStdDiscPercent' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("retailAddDiscPercent"), "'retailAddDiscPercent' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("retailFreightCharges"), "'retailFreightCharges' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("retailVatPercent"), "'retailVatPercent' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("state"), "'state' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("country"), "'country' key is missing");
                            Assert.assertTrue(exShowroomPrice.containsKey("currency"), "'currency' key is missing");
                            Assert.assertFalse(exShowroomPrice.containsKey("tenantId"), "'tenantId' key should not be present");
                        }
                        Assert.assertEquals(currency, country.equals("GB") ? "GBP" : "EUR", "Currency mismatch for partId: " + partNumber);                        priceFoundCount++;
                    }
                    logger.info("Tenant: {} | Parts with price: {} | Parts without price: {}", tenantId, priceFoundCount, priceNotFoundCount);
                }
            }


    @Test(description = "Validate Product merchandise details from DB and API response")
    public void validateProductMerchandiseDetails() throws Exception {
        List<String> tenantIds = DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.products_mna_new");
        for (String tenantId : tenantIds) {
            logger.info("Starting test: validateProductMerchandiseDetails for tenant: {}", tenantId);
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);

            List<String> partIds = new ArrayList<>();
            List<HashMap<String, Object>> rows = Db.executeQuery("select * from mdp.products_mna_new where country='" + tenantId + "'");
            logger.info("Tenant: {} | Total part numbers fetched from DB: {}", tenantId, rows.size());

            Set<String> uniquePartNumbers = new HashSet<>();
            Set<String> duplicatePartNumbers = new HashSet<>();
            for (HashMap<String, Object> row : rows) {
                Object partIdObj = row.get("sap_part_no");
                if (partIdObj != null) {
                    String partNo = partIdObj.toString();
                    if (!uniquePartNumbers.add(partNo)) {
                        duplicatePartNumbers.add(partNo);
                    }
                }
            }
            if (!duplicatePartNumbers.isEmpty()) {
                logger.warn("Tenant: {} | Duplicate sapPartNumbers found: {}", tenantId, duplicatePartNumbers);
            }
            Assert.assertTrue(duplicatePartNumbers.isEmpty(), "Tenant: " + tenantId + " | Duplicate sapPartNumbers found: " + duplicatePartNumbers);

            int totalNullPartNoRef = 0;
            int totalMappingMismatch = 0;
            int totalProcessed = 0;

            List<String> mismatchDetails = new ArrayList<>();
            Map<String, ReferenceStats> refStats = new HashMap<>();

            for (HashMap<String, Object> row : rows) {
                Object partIdObj = row.get("sap_part_no");
                if (partIdObj != null) {
                    partIds.add(partIdObj.toString());
                }
            }

            for (String partNumber : partIds) {
                partNumber = partNumber.replace("'", "");
                String requestUrl = BASE_API_URL + "/product/mna/norton/" + partNumber;
                logger.info("Tenant: {} | Request URL: {}", tenantId, requestUrl);
                Response res = ApiClient.get(requestUrl, headers);
                res.then().spec(Specs.responseTimeUnder());
                res.then().spec(Specs.responseOK());

                String respPartId = res.jsonPath().getString("data.productMnaNewData.skus.sapPartId");
                String partNoRef = res.jsonPath().getString("data.productMnaNewData.skus.partNoRef");
                String Size = res.jsonPath().getString("data.productMnaNewData.skus.size");

                Map<String, Object> productMnaNewData = res.jsonPath().getMap("data.productMnaNewData");
                Assert.assertTrue(productMnaNewData.containsKey("tenantId"), "'tenantId' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(productMnaNewData.containsKey("partName"), "'partName' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(productMnaNewData.containsKey("description"), "'description' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(productMnaNewData.containsKey("deliveryAndReturn"), "'deliveryAndReturn' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(productMnaNewData.containsKey("department"), "'department' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(productMnaNewData.containsKey("category"), "'category' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(productMnaNewData.containsKey("subCategory"), "'subCategory' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(productMnaNewData.containsKey("country"), "'country' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(productMnaNewData.containsKey("bulletPoints"), "'bulletPoints' key is missing for partNumber: " + partNumber);

                Map<String, Object> skus = (Map<String, Object>) productMnaNewData.get("skus");
                Assert.assertNotNull(skus, "'skus' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("sapPartId"), "'skus.sapPartId' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("eanBarcode"), "'skus.eanBarcode' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("colour"), "'skus.colour' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("size"), "'skus.size' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("displayName"), "'skus.displayName' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("partNoRef"), "'skus.partNoRef' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("linkToImage"), "'skus.linkToImage' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("moq"), "'skus.moq' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("availableForOrder"), "'skus.availableForOrder' key is missing for partNumber: " + partNumber);
                Assert.assertTrue(skus.containsKey("availableForSale"), "'skus.availableForSale' key is missing for partNumber: " + partNumber);

                String colourValue = skus.get("colour") != null ? skus.get("colour").toString().trim() : "";
                String sizeValue = skus.get("size") != null ? skus.get("size").toString().trim() : "";
                String displayNameValue = skus.get("displayName") != null ? skus.get("displayName").toString().trim() : "";

                if (colourValue.isEmpty()) {
                    logger.warn("Tenant: {} | Missing or empty 'colour' for partNumber: {}", tenantId, partNumber);
                }
                if (sizeValue.isEmpty()) {
                    logger.warn("Tenant: {} | Missing or empty 'size' for partNumber: {}", tenantId, partNumber);
                }
                if (displayNameValue.isEmpty()) {
                    logger.warn("Tenant: {} | Missing or empty 'displayName' for partNumber: {}", tenantId, partNumber);
                }

                Assert.assertEquals(respPartId, partNumber, "Part ID mismatch for partNumber: " + partNumber);

                String derivedPartRef = partNumber.length() > 9
                        ? partNumber.substring(0, 9)
                        : partNumber;

                String size = partNumber.length() > 9
                        ? partNumber.substring(9)
                        : "";
                totalProcessed++;

                if (partNoRef == null) {
                    totalNullPartNoRef++;
                }

                boolean mappingOK = (partNoRef != null && partNoRef.equals(derivedPartRef));
                if (!mappingOK) {
                    totalMappingMismatch++;
                    String msg = String.format(
                            "Mapping mismatch for PartID=%s | DerivedRef(from PartID)=%s | API.partNoRef=%s | Size=%s",
                            partNumber, derivedPartRef, String.valueOf(partNoRef), size
                    );
                    mismatchDetails.add(msg);
                    logger.warn("[MISMATCH] {}", msg);
                }

                ReferenceStats stats = refStats.computeIfAbsent(derivedPartRef, ReferenceStats::new);
                stats.partCount++;
                stats.sizes.add(size);
                if (!mappingOK) stats.mappingIssues++;
            }

            logger.info("\n==================== SUMMARY BY PART REFERENCE (Tenant: {}) ====================", tenantId);
            List<String> sortedRefs = new ArrayList<>(refStats.keySet());
            Collections.sort(sortedRefs);

            for (String ref : sortedRefs) {
                ReferenceStats s = refStats.get(ref);
                String sizeList = String.join(",", s.sizes.stream().sorted().collect(Collectors.toList()));
                logger.info(
                        "PartReference={} | DistinctSizes={} | Sizes=[{}] | PartCount={} | MappingIssues={}",
                        ref, s.sizes.size(), sizeList, s.partCount, s.mappingIssues
                );
            }

            if (!mismatchDetails.isEmpty()) {
                System.out.println("\n==================== MAPPING MISMATCH DETAILS (Tenant: " + tenantId + ") =====================");
                mismatchDetails.forEach(System.out::println);
            }
        }
    }

    @Test(description = "Pagination api for each tenant")
    public void TestFetchAllPartsWithValidation_pagenation_api() throws Exception
    {
        List<String> tenantIds = DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.vehicle_models");
        for (String tenantId : tenantIds) {
            logger.info("Starting pagination test for tenant: {}", tenantId);
            String baseUrl = BASE_API_URL + "/product/vehicle/details";
            int pageSize = 1;
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);
            Pagenationsize paginationClient = new Pagenationsize(baseUrl, pageSize, headers);
            ResponseSpecification responseSpec = Specs.responseOK();
            List<Response> responses = Collections.singletonList(paginationClient.Vechicalpagenation(responseSpec));
            List<String> allParts = new ArrayList<>();
            for (Response res : responses) {
                List<String> partsid = res.jsonPath().getList("data.vehicleParts.sapPartId", String.class);
                if (partsid != null) {
                    allParts.addAll(partsid);
                    logger.info("Tenant: {} | Fetched parts in this page: {}", tenantId, partsid);
                } else {
                    logger.info("Tenant: {} | No parts found in this page.", tenantId);
                }
                Boolean hasMore = res.jsonPath().getBoolean("data.hasMore");
                String nextCursor = res.jsonPath().getString("data.nextCursor");
                logger.info("Tenant: {} | Next cursor: {}, hasMore={}", tenantId, nextCursor, hasMore);
                assertThat("hasMore should not be null", hasMore, notNullValue());
            }
            logger.info("Tenant: {} | Total parts fetched: {}", tenantId, allParts.size());
            assertThat("Parts list should not be empty", allParts, not(empty()));
        }
    }


    @Test(description = "Proximity api test for each tenant")
    public void Postproximityapi() throws Exception {
        List<String> tenantIds = DBUtils.getTenantIdsForTest("SELECT DISTINCT tenant_id FROM mdp.vehicle_models");
        for (String tenantId : tenantIds) {
            logger.info("Starting Proximity test for tenant: {}", tenantId);
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", tenantId);
            try (InputStream file = new FileInputStream("src/test/resources/data/Proximity.json")) {
                String jsonBody = new String(file.readAllBytes());
                Response res = ApiClient.post(BASE_API_URL + "/dealers/listOfDealerDetails/basedOnProximity", jsonBody, headers);
                res.then().spec(Specs.responseTimeUnder());
                res.then().spec(Specs.responseOK());
                logger.info("Tenant: {} | Proximity API Response: {}", tenantId, res.asString());
                List<String> Delares = res.jsonPath().getList("data.dealerData.dealer.sapDealerCode");
                logger.info("Tenant: {} | Dealers list is: {}", tenantId, Delares);
                logger.info("Tenant: {} | Total number of dealers: {}", tenantId, Delares != null ? Delares.size() : 0);
            } catch (IOException e) {
                logger.error("Error reading Proximity.json file for tenant: {}", tenantId, e);
            }
        }
    }

    // Helper holder for per-reference aggregation
    static class ReferenceStats {
        final String partReference;
        final Set<String> sizes = new HashSet<>();
        int partCount = 0;
        int mappingIssues = 0;

        ReferenceStats(String partReference) {
            this.partReference = partReference;
        }
    }
}
