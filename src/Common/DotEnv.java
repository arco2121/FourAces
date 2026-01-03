package Common;

import java.io.FileInputStream;
import java.util.Properties;

public class DotEnv {
    private static final Properties props = new Properties();
    static {
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile caricare .env");
        }
    }
    public static String get(String key) {
        return props.getProperty(key);
    }
}
