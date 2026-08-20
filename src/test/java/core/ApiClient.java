package core;

import io.restassured.response.Response;
import utils.TokenManager;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiClient {
    // public static Response get(String path,Map<String, String> headers) {
  //     return given().spec(Specs.request())
  //           .header(headers);
//
//                .when().get(path)
//                .then().extract().response();
  //  }}
    public static Response get(String path, Map<String, String> headers) {
        return given()
                .spec(Specs.request())
                .headers(headers)
                .when().get(path)
                .then().extract().response();
    }


    //    public static Response post(String path, Object body) {
//        return given().spec(Specs.request())
//                //.header("X-Tenant-ID", "GB")
//                //.header("Authorization", "Bearer " + TokenManager.getToken())
//                .body(body)
//                .when().post(path)
//                .then().extract().response();
//    }
    public static Response post(String path, Object body, Map<String, String> headers) {
        return given()
                .spec(Specs.request())
                .headers(headers)
                .body(body)
                .when().post(path)
                .then().extract().response();
    }

//    public static Response getDealerDetailes(String path) {
//       return given().spec(Specs.request())
//              // .header("Authorization", "Bearer " + TokenManager.getToken())
//                .when().get(path)
//                .then().extract().response();
//
//    }


    public static Response getDealerDetailes(String path, Map<String, String> headers) {
        return given()
                .spec(Specs.request())
                .headers(headers)
                .when().get(path)
                .then().extract().response();

    }
}