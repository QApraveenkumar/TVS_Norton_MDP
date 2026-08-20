package Pojo;
import core.ApiClient;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pagenationsize {
    private final String pagebaseUrl;
    private final int pagepageSizes;
    private final Map<String, String> headers;

    public Pagenationsize(String pagebaseUrl, int pagepageSizes, Map<String, String> headers) {
        this.pagebaseUrl = pagebaseUrl;
        this.pagepageSizes = pagepageSizes;
        this.headers = headers != null ? headers : new java.util.HashMap<>();
    }

    public List<Response> fetchAllResponses(ResponseSpecification responseSpec) {
        String currentCursor = null;
        boolean hasMore = true;
        List<Response> allResponses = new ArrayList<>();

        while (hasMore) {
            String url = pagebaseUrl + "?cursor=" + (currentCursor == null ? "" : currentCursor) + "&size=" + pagepageSizes;

            Response res = ApiClient.get(url, headers)
                    .then()
                    .spec(responseSpec)
                    .extract()
                    .response();

            allResponses.add(res);

            currentCursor = res.jsonPath().getString("data.nextCursor");
            hasMore = res.jsonPath().getBoolean("data.hasMore");
        }

        return allResponses;
    }

    public Response Vechicalpagenation(ResponseSpecification responseSpec) {
        List<Response> allResponses = fetchAllResponses(responseSpec);
        return allResponses.get(allResponses.size() - 1);
    }
}
