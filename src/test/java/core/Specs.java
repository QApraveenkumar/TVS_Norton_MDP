package core;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import utils.Config;
import utils.TokenManager;

import java.io.IOException;

import static org.hamcrest.Matchers.lessThan;

    public class Specs {
    private  static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;

    public static RequestSpecification request() {
        if (requestSpec == null) {
            RequestSpecBuilder b = new RequestSpecBuilder()
                    .setBaseUri(Config.get("baseUrl"));
            System.out.println("Base URI used: " + Config.get("baseUrl"));
                   b.setAccept(ContentType.JSON)
                    .setContentType(ContentType.JSON)
                    .setRelaxedHTTPSValidation();

            String token = TokenManager.getToken();
            System.out.println("Token used: " + token);
            if (token != null && !token.isBlank()) {
                b.addHeader("Authorization", "Bearer " + token);
            }

            if (Config.getBool("logRequests")) {
                b.addFilter(new RequestLoggingFilter());
                b.addFilter(new ResponseLoggingFilter());
            }
            requestSpec = b.build();
        }
        return requestSpec;
    }


    public static ResponseSpecification responseOK() {
        if (responseSpec == null) {
            responseSpec = new ResponseSpecBuilder()
                    .expectStatusCode(200)
                    .expectContentType(ContentType.JSON)
                    .build();
        }
        return responseSpec;
    }

    public static ResponseSpecification responseTimeUnder() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan((long) Config.getInt("responseTimeMs")))
                .build();
}}



