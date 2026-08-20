package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBUtils {
    public static List<String> getTenantIdsForTest(String sql) throws Exception {
        List<HashMap<String, Object>> rows = Db.executeQuery(sql);
        List<String> tenantIds = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            tenantIds.add(row.get("tenant_id").toString());
        }
        return tenantIds;
    }

}
