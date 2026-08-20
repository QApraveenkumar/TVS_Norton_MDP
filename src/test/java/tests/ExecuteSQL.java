package tests;

                import org.testng.annotations.AfterClass;
                import org.testng.annotations.BeforeClass;
                import org.testng.annotations.Test;
                import java.io.FileWriter;
                import java.io.IOException;
                import java.util.*;

                public class ExecuteSQL {

                    private final List<Map<String, Object>> reportRows = new ArrayList<>();

                    @BeforeClass
                    public void setup() {
                        // Setup DB connection if needed
                    }

                    @Test(description = "Automated Data Validation Scenarios")
                    public void runAllScenarios() {
                        runScenario("1. Tenant-wise Model Count",
                                "SELECT tenant_id, COUNT(DISTINCT model_id) AS total_models FROM mdp.vehicle_models GROUP BY tenant_id ORDER BY tenant_id;");
                        runScenario("2. Tenant → Brand Mapping",
                                "SELECT tenant_id, brand, COUNT(DISTINCT model_id) AS model_count FROM mdp.vehicle_models GROUP BY tenant_id, brand ORDER BY tenant_id, brand;");
                        runScenario("3. Brand in Multiple Tenants",
                                "SELECT brand, COUNT(DISTINCT tenant_id) AS tenant_count FROM mdp.vehicle_models GROUP BY brand HAVING COUNT(DISTINCT tenant_id) > 1;");
                        runScenario("4. Variants per Brand (Tenant wise)",
                                "SELECT tenant_id, brand, COUNT(DISTINCT model_id) AS variant_count FROM mdp.vehicle_models GROUP BY tenant_id, brand;");
                        runScenario("5. Duplicate Models within Tenant",
                                "SELECT tenant_id, sap_model_id, COUNT(*) AS duplicate_count FROM mdp.vehicle_models GROUP BY tenant_id, sap_model_id HAVING COUNT(*) > 1;");
                        runScenario("6. Tenant-wise Parts Count",
                                "SELECT vm.tenant_id, COUNT(DISTINCT vp.sap_part_id) AS total_parts FROM mdp.vehicle_parts vp JOIN mdp.vehicle_models vm ON vp.model_id = vm.model_id GROUP BY vm.tenant_id;");
                        runScenario("7. Duplicate Parts within Tenant",
                                "SELECT vm.tenant_id, vp.sap_part_id, COUNT(*) AS duplicate_count FROM mdp.vehicle_parts vp JOIN mdp.vehicle_models vm ON vp.model_id = vm.model_id GROUP BY vm.tenant_id, vp.sap_part_id HAVING COUNT(*) > 1;");
                        runScenario("8. Parts Mapped to Multiple Models",
                                "SELECT vm.tenant_id, vp.sap_part_id, COUNT(DISTINCT vp.model_id) AS model_count FROM mdp.vehicle_parts vp JOIN mdp.vehicle_models vm ON vp.model_id = vm.model_id GROUP BY vm.tenant_id, vp.sap_part_id HAVING COUNT(DISTINCT vp.model_id) > 1;");
                        runScenario("9. Parts Availability (Sales/Service)",
                                "SELECT vm.tenant_id, vm.model_name, SUM(CASE WHEN vp.is_sales_order = 1 THEN 1 ELSE 0 END) AS sales_parts, SUM(CASE WHEN vp.is_service_part = 1 THEN 1 ELSE 0 END) AS service_parts FROM mdp.vehicle_parts vp JOIN mdp.vehicle_models vm ON vp.model_id = vm.model_id GROUP BY vm.tenant_id, vm.model_name;");
                        runScenario("10. Duplicate Part Description",
                                "SELECT vm.tenant_id, vm.brand, vp.part_description, COUNT(DISTINCT vp.sap_part_id) AS part_count FROM mdp.vehicle_parts vp JOIN mdp.vehicle_models vm ON vp.model_id = vm.model_id GROUP BY vm.tenant_id, vm.brand, vp.part_description HAVING COUNT(DISTINCT vp.sap_part_id) > 1;");
                        runScenario("11. Models Without Parts",
                                "SELECT vm.tenant_id, vm.model_id, vm.model_name FROM mdp.vehicle_models vm LEFT JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id WHERE vp.model_id IS NULL;");
                        runScenario("12. Parts Without Valid Model",
                                "SELECT vp.sap_part_id, vp.model_id FROM mdp.vehicle_parts vp LEFT JOIN mdp.vehicle_models vm ON vp.model_id = vm.model_id WHERE vm.model_id IS NULL;");
                    }

                    private void runScenario(String scenario, String sql) {
                        try {
                            List<HashMap<String, Object>> rows = utils.Db.executeQuery(sql);
                            if (rows.isEmpty()) {
                                Map<String, Object> noResult = new LinkedHashMap<>();
                                noResult.put("Scenario", scenario);
                                noResult.put("Result", "No records found");
                                reportRows.add(noResult);
                            } else {
                                for (HashMap<String, Object> row : rows) {
                                    Map<String, Object> result = new LinkedHashMap<>();
                                    result.put("Scenario", scenario);
                                    result.putAll(row);
                                    reportRows.add(result);
                                }
                            }
                        } catch (Exception e) {
                            Map<String, Object> errorResult = new LinkedHashMap<>();
                            errorResult.put("Scenario", scenario);
                            errorResult.put("Error", e.getMessage());
                            reportRows.add(errorResult);
                        }

                    }

                    @AfterClass
                    public void printReport() {
                        if (reportRows.isEmpty()) {
                            System.out.println("All validations passed. No issues found.");
                            return;
                        }
                        // Print as table
                        Set<String> headers = new LinkedHashSet<>();
                        for (Map<String, Object> row : reportRows) headers.addAll(row.keySet());
                        System.out.println(String.join(" | ", headers));
                        System.out.println(new String(new char[120]).replace("\0", "-"));
                        for (Map<String, Object> row : reportRows) {
                            List<String> values = new ArrayList<>();
                            for (String h : headers) values.add(String.valueOf(row.getOrDefault(h, "")));
                            System.out.println(String.join(" | ", values));
                        }
                        // Export to CSV
                        try (FileWriter writer = new FileWriter("validation_report.csv")) {
                            writer.write(String.join(",", headers) + "\n");
                            for (Map<String, Object> row : reportRows) {
                                List<String> values = new ArrayList<>();
                                for (String h : headers) values.add(String.valueOf(row.getOrDefault(h, "")));
                                writer.write(String.join(",", values) + "\n");
                            }
                            System.out.println("Report exported to validation_report.csv");
                        } catch (IOException e) {
                            System.err.println("Failed to export report: " + e.getMessage());
                        }

                    }
                    // Dealer Master Table – Validation Scenarios

                    @Test(description = "Dealer Master Table – Validation Scenarios")
                    public void dealerMasterValidations() {
                        runScenario("D1. Total Dealer Count",
                                "SELECT COUNT(*) AS total_dealers FROM mdp.dealers;");
                        runScenario("D2. Tenant-wise Dealer Count",
                                "SELECT tenant_id, COUNT(*) AS dealer_count FROM mdp.dealers GROUP BY tenant_id ORDER BY tenant_id;");
                        runScenario("D3. Dealer Type Distribution (Tenant-wise)",
                                "SELECT tenant_id, type, COUNT(*) AS type_count FROM mdp.dealers GROUP BY tenant_id, type ORDER BY tenant_id, type;");
                        runScenario("D4. Dealers by SAP Status",
                                "SELECT sap_status, COUNT(*) AS count FROM mdp.dealers GROUP BY sap_status;");
                        runScenario("D4b. Dealers by SAP Status (Tenant-wise)",
                                "SELECT tenant_id, sap_status, COUNT(*) AS count FROM mdp.dealers GROUP BY tenant_id, sap_status ORDER BY tenant_id, sap_status;");
                        runScenario("D5. Dealers by MDP Status",
                                "SELECT tenant_id, mdp_status, COUNT(*) AS count FROM mdp.dealers GROUP BY tenant_id, mdp_status;");
                        runScenario("D6. Duplicate Dealer SAP Codes (Should be Zero)",
                                "SELECT sap_dealer_code, COUNT(*) AS code_count FROM mdp.dealers GROUP BY sap_dealer_code HAVING COUNT(*) > 1;");
                                runScenario("D7. Dealers Without SAP Code (Invalid Records)",
                                        "SELECT * FROM mdp.dealers WHERE sap_dealer_code IS NULL OR sap_dealer_code = '';");
                                    // Dealer Location Table – Validation Scenarios

                                                     runScenario("DL2b. Count of Dealer Locations with Latitude/Longitude Missing",
                                                             "SELECT COUNT(*) AS dealers_missing_lat_long FROM mdp.dealer_locations WHERE latitude IS NULL OR longitude IS NULL OR latitude = '' OR longitude = '';");
                                                     runScenario("DL3. Total Dealers with Latitude & Longitude (Geo-Tagged)",
                                                             "SELECT COUNT(*) AS dealers_with_lat_long FROM mdp.dealer_locations WHERE latitude IS NOT NULL AND longitude IS NOT NULL AND latitude <> '' AND longitude <> '';");
                                                    }

                // Merchandise Products Table – Validation Scenarios
                    @Test(description = "Merchandise Products Table – Validation Scenarios")
                    public void mechabdiesTest() {
                        runScenario("MMerchandise1. Total SAP Part Count",
                                "SELECT COUNT(sap_part_no) AS total_sap_parts FROM products_mna_new;");
                        runScenario("Merchandise2. Tenant-wise SAP Part Count",
                                "SELECT tenant_id, COUNT(sap_part_no) AS sap_parts_count FROM mdp.products_mna_new GROUP BY tenant_id ORDER BY tenant_id;");
                        runScenario("Merchandise3. Null Count for cate (Sub Category)",
                                "SELECT COUNT(*) AS null_cate_count FROM mdp.products_mna_new WHERE cate IS NULL OR cate = '';");
                        runScenario("Merchandise4. Null Count for cate_2 (Colour)",
                                "SELECT COUNT(*) AS null_colour_count FROM mdp.products_mna_new WHERE cate_2 IS NULL OR cate_2 = '';");
                        runScenario("Merchandise5. Null Count for cate_3 (Part Reference – Level 1)",
                                "SELECT COUNT(*) AS null_partref_lvl1 FROM mdp.products_mna_new WHERE cate_3 IS NULL OR cate_3 = '';");
                        runScenario("Merchandise6. Null Count for cate_4 (Size)",
                                "SELECT COUNT(*) AS null_size_count FROM mdp.products_mna_new WHERE cate_4 IS NULL OR cate_4 = '';");
                        runScenario("Merchandise7. Null Count for category (Main Category)",
                                "SELECT COUNT(*) AS null_main_category_count FROM mdp.products_mna_new WHERE category IS NULL OR category = '';");
                        runScenario("Merchandise8. Null MOQ (Minimum Order Quantity)",
                                "SELECT COUNT(*) AS null_moq_count FROM mdp.products_mna_new WHERE moq IS NULL;");
                        runScenario("Merchandise9. Combined Null Summary for All Critical Fields",
                                "SELECT " +
                                "COUNT(CASE WHEN cate IS NULL OR cate = '' THEN 1 END) AS null_cate, " +
                                "COUNT(CASE WHEN cate_2 IS NULL OR cate_2 = '' THEN 1 END) AS null_cate_2, " +
                                "COUNT(CASE WHEN cate_3 IS NULL OR cate_3 = '' THEN 1 END) AS null_cate_3, " +
                                "COUNT(CASE WHEN cate_4 IS NULL OR cate_4 = '' THEN 1 END) AS null_cate_4, " +
                                "COUNT(CASE WHEN category IS NULL OR category = '' THEN 1 END) AS null_category, " +
                                "COUNT(CASE WHEN moq IS NULL THEN 1 END) AS null_moq " +
                                "FROM mdp.products_mna_new;");
                        runScenario("M10. Duplicate SAP Part Numbers Within the Same Tenant (Should be Zero)",
                                "SELECT tenant_id, sap_part_no, COUNT(*) AS duplicate_count FROM mdp.products_mna_new GROUP BY tenant_id, sap_part_no HAVING COUNT(*) > 1;");

                   runScenario("M11. Unique Tenant & Country Combinations",
                           "SELECT tenant_id, country, COUNT(*) AS product_count FROM mdp.products_mna_new GROUP BY tenant_id, country ORDER BY tenant_id, country;");
                    }

@Test(description = "Product Price Table – Validation Scenarios")
                        public void productPriceValidations() {
                            // 1. Total Parts That Have Price Data
                            runScenario("PPrice1. Total Parts With Price Data",
                                    "SELECT COUNT( part_id) AS total_parts_with_price FROM mdp.product_price;");

                            // 2. Tenant-wise Parts With Price Data
                            runScenario("PPrice2. Tenant-wise Parts With Price Data",
                                    "SELECT pld.tenant_id, COUNT(DISTINCT pp.part_id) AS parts_with_price " +
                                    "FROM mdp.product_price pp " +
                                    "JOIN mdp.price_location_details pld ON pp.location_id = pld.id " +
                                    "GROUP BY pld.tenant_id ORDER BY pld.tenant_id;");

                            // 3. Count HOW MANY PARTS HAVE ZERO PRICE VALUES
                            runScenario("PPrice3. Parts With Zero Price Values",
                                    "SELECT " +
                                    "COUNT(CASE WHEN dealer_price = 0 THEN 1 END) AS zero_dealer_price, " +
                                    "COUNT(CASE WHEN retail_price = 0 THEN 1 END) AS zero_retail_price, " +
                                    "COUNT(CASE WHEN dealer_std_disc_percent = 0 THEN 1 END) AS zero_std_disc, " +
                                    "COUNT(CASE WHEN dealer_add_disc_percent = 0 THEN 1 END) AS zero_add_disc, " +
                                    "COUNT(CASE WHEN retail_std_disc_percent = 0 THEN 1 END) AS zero_retail_std_disc, " +
                                    "COUNT(CASE WHEN retail_add_disc_percent = 0 THEN 1 END) AS zero_retail_add_disc, " +
                                    "COUNT(CASE WHEN dealer_freight_charges = 0 THEN 1 END) AS zero_dealer_freight, " +
                                    "COUNT(CASE WHEN retail_freight_charges = 0 THEN 1 END) AS zero_retail_freight, " +
                                    "COUNT(CASE WHEN dealer_vat_percent = 0 THEN 1 END) AS zero_dealer_vat, " +
                                    "COUNT(CASE WHEN retail_vat_percent = 0 THEN 1 END) AS zero_retail_vat " +
                                    "FROM mdp.product_price;");

                            // 4. Count HOW MANY PARTS HAVE NON-ZERO PRICE VALUES
                            runScenario("PPrice4. Parts With Non-Zero Price Values",
                                    "SELECT " +
                                    "COUNT(CASE WHEN dealer_price > 0 THEN 1 END) AS nonzero_dealer_price, " +
                                    "COUNT(CASE WHEN retail_price > 0 THEN 1 END) AS nonzero_retail_price, " +
                                    "COUNT(CASE WHEN dealer_std_disc_percent > 0 THEN 1 END) AS nonzero_std_disc, " +
                                    "COUNT(CASE WHEN dealer_add_disc_percent > 0 THEN 1 END) AS nonzero_add_disc, " +
                                    "COUNT(CASE WHEN retail_std_disc_percent > 0 THEN 1 END) AS nonzero_retail_std_disc, " +
                                    "COUNT(CASE WHEN retail_add_disc_percent > 0 THEN 1 END) AS nonzero_retail_add_disc, " +
                                    "COUNT(CASE WHEN dealer_freight_charges > 0 THEN 1 END) AS nonzero_dealer_freight, " +
                                    "COUNT(CASE WHEN retail_freight_charges > 0 THEN 1 END) AS nonzero_retail_freight, " +
                                    "COUNT(CASE WHEN dealer_vat_percent > 0 THEN 1 END) AS nonzero_dealer_vat, " +
                                    "COUNT(CASE WHEN retail_vat_percent > 0 THEN 1 END) AS nonzero_retail_vat " +
                                    "FROM mdp.product_price;");

                            // 5. Tenant-wise Zero and Non-Zero Price Summary
                            runScenario("PPrice5. Tenant-wise Zero and Non-Zero Price Summary",
                                    "SELECT pld.tenant_id, " +
                                    "COUNT(CASE WHEN pp.dealer_price = 0 THEN 1 END) AS zero_dealer_price, " +
                                    "COUNT(CASE WHEN pp.dealer_price > 0 THEN 1 END) AS nonzero_dealer_price, " +
                                    "COUNT(CASE WHEN pp.retail_price = 0 THEN 1 END) AS zero_retail_price, " +
                                    "COUNT(CASE WHEN pp.retail_price > 0 THEN 1 END) AS nonzero_retail_price " +
                                    "FROM mdp.product_price pp " +
                                    "JOIN mdp.price_location_details pld ON pp.location_id = pld.id " +
                                    "GROUP BY pld.tenant_id ORDER BY pld.tenant_id;");

                            // 6. Count parts with price but missing location mapping
                            runScenario("PPrice6. Parts With Price But Missing Location Mapping",
                                    "SELECT COUNT(*) AS missing_location_mapping " +
                                    "FROM mdp.product_price pp " +
                                    "LEFT JOIN mdp.price_location_details pld ON pp.location_id = pld.id " +
                                    "WHERE pld.id IS NULL;");

                            // 7. Count parts with multiple price entries
                            runScenario("PPrice7. Parts With Multiple Price Entries",
                                    "SELECT part_id, COUNT(*) AS price_entries " +
                                    "FROM mdp.product_price " +
                                    "GROUP BY part_id HAVING COUNT(*) > 1;");
                        }

// Vehicle Parts & Pricing Table – Validation Scenarios
    @Test(description = "Vehicle Parts & Pricing Table – Validation Scenarios")
    public void vehiclePartsPricingValidations() {
        runScenario("VP1. Total Vehicle Parts Count",
                "SELECT COUNT(DISTINCT part_id) AS total_vehicle_parts FROM mdp.vehicle_parts;");
        runScenario("VP2. Total Priced Parts Count",
                "SELECT COUNT(DISTINCT part_id) AS total_price_parts FROM mdp.price_vehicle_model;");
        runScenario("VP3. Tenant-wise Parts Count",
                "SELECT vm.tenant_id, COUNT(DISTINCT vp.part_id) AS total_parts FROM mdp.vehicle_parts vp JOIN mdp.vehicle_models vm ON vp.model_id = vm.model_id GROUP BY vm.tenant_id ORDER BY vm.tenant_id;");
        runScenario("VP4. Tenant-wise Priced Parts Count",
                "SELECT pld.tenant_id, COUNT(DISTINCT pvm.part_id) AS priced_parts FROM mdp.price_vehicle_model pvm JOIN mdp.price_location_details pld ON pvm.location_id = pld.id GROUP BY pld.tenant_id ORDER BY pld.tenant_id;");
        runScenario("VP5. Common Parts Between Parts & Pricing",
                "SELECT COUNT(DISTINCT vp.part_id) AS common_parts FROM mdp.vehicle_parts vp JOIN mdp.price_vehicle_model pvm ON vp.part_id = pvm.part_id;");
        runScenario("VP6. Parts Without Price",
                "SELECT COUNT(DISTINCT vp.part_id) AS parts_without_price FROM mdp.vehicle_parts vp LEFT JOIN mdp.price_vehicle_model pvm ON vp.part_id = pvm.part_id WHERE pvm.part_id IS NULL;");
        runScenario("VP7. Orphan Price Records (Parts With Price But Not Present in Vehicle Parts)",
                "SELECT COUNT(DISTINCT pvm.part_id) AS orphan_price_parts FROM mdp.price_vehicle_model pvm LEFT JOIN mdp.vehicle_parts vp ON pvm.part_id = vp.part_id WHERE vp.part_id IS NULL;");
        runScenario("VP8. Parts With Zero Dealer Price count",
                "SELECT COUNT(*) AS zero_dealer_price FROM mdp.price_vehicle_model WHERE dealer_price = null;");
        runScenario("VP9. Parts With Zero Retail Price",
                "SELECT COUNT(*) AS zero_retail_price FROM mdp.price_vehicle_model WHERE retail_price = null;");
    }

@Test(description = "Labour Table – Validation Scenarios")
    public void labourTableValidations() {
        // 1. Count of Distinct Brands and Their IDs
        runScenario("L1. Distinct Brands and Brand Codes",
                "SELECT brand, brand_code, COUNT(*) AS total_records FROM mdp.labour GROUP BY brand, brand_code ORDER BY brand;");

        // 2. Count of Distinct Labour Area IDs for Each Brand
        runScenario("L2. Distinct Labour Area IDs per Brand",
                "SELECT brand, COUNT(DISTINCT labour_area_id) AS distinct_labour_area_count FROM mdp.labour GROUP BY brand ORDER BY brand;");
        runScenario("L2b. Labour Area Details per Brand",
                "SELECT brand, labour_area_id, labour_area, COUNT(*) AS total_records FROM mdp.labour GROUP BY brand, labour_area_id, labour_area ORDER BY brand, labour_area_id;");

        // 3. Count of Labour Activities for Each Labour Area Based on Brand
        runScenario("L3. Distinct Labour Activities per Area and Brand",
                "SELECT brand, labour_area_id, labour_area, COUNT(DISTINCT labour_activity_id) AS distinct_activity_count FROM mdp.labour GROUP BY brand, labour_area_id, labour_area ORDER BY brand, labour_area_id;");
        runScenario("L3b. Labour Activity Details",
                "SELECT brand, labour_area, labour_activity_id, labour_activity FROM mdp.labour ORDER BY brand, labour_area, labour_activity_id;");

        // 4. Total Count of Labour Data
        runScenario("L4. Total Labour Records",
                "SELECT COUNT(*) AS total_labour_records FROM mdp.labour;");

        // 5. Labour Records Having 0 Minutes
        runScenario("L5. Labour Records with 0 Minutes",
                "SELECT id, brand, labour_area, labour_activity, labour_duration_in_minutes FROM mdp.labour WHERE labour_duration_in_minutes = 0;");

        // 6. Count of 0 Minute Labour Records by Brand
        runScenario("L6. 0 Minute Labour Records by Brand",
                "SELECT brand, COUNT(*) AS zero_minute_count FROM mdp.labour WHERE labour_duration_in_minutes = 0 GROUP BY brand;");

        // 7. Duplicate Labour Activity Validation
        runScenario("L7. Duplicate Labour Activity Validation",
                "SELECT brand, labour_area_id, labour_activity_id, COUNT(*) AS duplicate_count FROM mdp.labour GROUP BY brand, labour_area_id, labour_activity_id HAVING COUNT(*) > 1;");

        // 8. Null Validation Query
        runScenario("L8. Null Validation for Mandatory Fields",
                "SELECT * FROM mdp.labour WHERE brand IS NULL OR labour_area_id IS NULL OR labour_activity_id IS NULL OR labour_duration_in_minutes IS NULL;");

        // 9. Negative Duration Validation
        runScenario("L9. Negative Labour Duration Validation",
                "SELECT * FROM mdp.labour WHERE labour_duration_in_minutes < 0;");
    }

@Test(description = "Automated Tenant-wise Parts Validation Scenarios")
    public void tenantWisePartsValidations() {
        runScenario("1. Tenant-wise Parts Count",
                "SELECT vm.tenant_id, COUNT(vp.part_id) AS parts_count FROM mdp.vehicle_models vm JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id GROUP BY vm.tenant_id ORDER BY parts_count DESC;");
        runScenario("2. Tenant-wise Parts List",
                "SELECT vm.tenant_id, vp.part_id, vp.part_name, vp.part_description, vp.model_id, vp.available_for_sale, vp.available_for_service, vp.available_for_order FROM mdp.vehicle_models vm JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id ORDER BY vm.tenant_id, vp.part_id;");
        runScenario("3. Tenant + Model-wise Parts Count",
                "SELECT vm.tenant_id, vm.model_id, vm.model_name, COUNT(vp.part_id) AS parts_count FROM mdp.vehicle_models vm LEFT JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id GROUP BY vm.tenant_id, vm.model_id, vm.model_name ORDER BY vm.tenant_id, parts_count DESC;");
        runScenario("4. Tenant-wise Available Parts (Sales Scenario)",
                "SELECT vm.tenant_id, COUNT(vp.part_id) AS sale_parts_count FROM mdp.vehicle_models vm JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id WHERE vp.available_for_sale = 1 GROUP BY vm.tenant_id;");
        runScenario("5. Data Quality Scenario (Missing Mapping)",
                "SELECT vp.* FROM mdp.vehicle_parts vp LEFT JOIN mdp.vehicle_models vm ON vp.model_id = vm.model_id WHERE vm.model_id IS NULL;");
        runScenario("6. Data Quality Scenario (Duplicate Parts)",
                "SELECT vm.tenant_id, vp.part_id, COUNT(*) AS duplicate_count FROM mdp.vehicle_models vm JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id GROUP BY vm.tenant_id, vp.part_id HAVING COUNT(*) > 1;");
        runScenario("7. Tenant-wise Parts with No Availability",
                "SELECT vm.tenant_id, vp.part_id, vp.part_name FROM mdp.vehicle_models vm JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id WHERE vp.available_for_sale = 0 AND vp.available_for_service = 0 AND vp.available_for_order = 0;");
        runScenario("8. Tenant-wise NULL Data Check",
                "SELECT vm.tenant_id, COUNT(*) AS null_records FROM mdp.vehicle_models vm JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id WHERE vp.part_name IS NULL OR vp.part_id IS NULL GROUP BY vm.tenant_id;");
        runScenario("Summary: Tenant-wise Model & Parts Breakdown",
                "SELECT vm.tenant_id, COUNT(DISTINCT vm.model_id) AS total_models, COUNT(vp.part_id) AS total_parts, SUM(CASE WHEN vp.available_for_sale = 1 THEN 1 ELSE 0 END) AS sale_parts, SUM(CASE WHEN vp.available_for_service = 1 THEN 1 ELSE 0 END) AS service_parts FROM mdp.vehicle_models vm LEFT JOIN mdp.vehicle_parts vp ON vm.model_id = vp.model_id GROUP BY vm.tenant_id;");
    }

                }
