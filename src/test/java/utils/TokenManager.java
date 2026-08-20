package utils;

import io.restassured.response.Response;

import java.util.Properties;

import static io.restassured.RestAssured.given;

public class TokenManager {
    // private static final Properties props = new Properties();

    private static String token;
    private static long expiryTime;

    public static String getToken() {
        if (token != null && System.currentTimeMillis() < expiryTime) {
            return token;
        }

        Response res = given()
                .baseUri(Config.get("tokenUrl"))
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", Config.get("clientId"))
                .formParam("client_secret", Config.get("clientSecret"))
                .formParam("scope", Config.get("scope"))
                .post("/oauth2/v2.0/token");

        token = res.jsonPath().getString("access_token");
        Integer expiresIn = res.jsonPath().getInt("expires_in");
        if (token == null || expiresIn == null) {
            throw new RuntimeException("Token or expires_in not found in response: " + res.asString());
        }
        expiryTime = System.currentTimeMillis() + (expiresIn - 30) * 1000;
        return token;
    }
}