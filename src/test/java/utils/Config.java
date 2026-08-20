package utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
public class Config {
    static final Properties props = new Properties();


    static {
        String env = System.getProperty("env", "prod");
        String file = String.format("/config/%s.properties", env);
        try (InputStream is = Config.class.getResourceAsStream(file)) {
            if (is == null) throw new RuntimeException("Config file not found: " + file);
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + e.getMessage(), e);
        }
    }


    public static String get(String key) { return props.getProperty(key); }
    public static int getInt(String key) { return Integer.parseInt(props.getProperty(key)); }
    public static boolean getBool(String key) { return Boolean.parseBoolean(props.getProperty(key)); }
}

