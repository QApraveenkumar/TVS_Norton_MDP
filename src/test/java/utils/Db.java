package utils;

import org.testng.annotations.*;

import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utils.Config.props;

public class Db {
private static Connection connection;

    // ✅ Connect to DB before tests
     @BeforeClass(alwaysRun = true)
      public static void startConnect() throws Exception {
          try (FileInputStream fis = new FileInputStream("src/test/resources/config/ProDB.properties")) {
              Config.props.load(fis);
              System.out.println("file retrive sucess fully");
          }
          if (connection == null || connection.isClosed()) {
              String url = Config.get("db.url");
              String user = Config.get("db.username");
              String pass = Config.get("db.password");
              connection = DriverManager.getConnection(url, user, pass);
              System.out.println("✅ DB Connected Successfully");
          }
      }

  // ✅ Run a SELECT query and return list of rows (each row as Map<column,value>)
  public static List<HashMap<String, Object>> executeQuery(String query) throws Exception {
         startConnect();
      if (connection == null || connection.isClosed()) {
          throw new IllegalStateException("DB connection is not established. Call startConnect() first.");
      }
      List<HashMap<String, Object>> results = new ArrayList<>();
      try (Statement stmt = connection.createStatement();
           ResultSet rs = stmt.executeQuery(query)) {
          ResultSetMetaData meta = rs.getMetaData();
          int colCount = meta.getColumnCount();
          while (rs.next()) {
              HashMap<String, Object> row = new HashMap<>();
              for (int i = 1; i <= colCount; i++) {
                  row.put(meta.getColumnLabel(i), rs.getObject(i));
              }
              results.add(row);
          }
          closeConnect();
      }
      return results;
  }
@AfterClass
// ✅ Close DB connection after all tests
  public static void closeConnect() throws Exception {
      if (connection != null && !connection.isClosed()) {
          connection.close();
          System.out.println("🔒 DB Connection Closed");
      }
  }

}

