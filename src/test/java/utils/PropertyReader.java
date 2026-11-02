package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads <env>.properties from src/test/resources.
 * Pick the environment with: mvn test -Denv=qa   (default is qa)
 */
public class PropertyReader {

    private static final Properties props = new Properties();

    static {
        String env = System.getProperty("env", "qa");
        String file = env + ".properties";
        try (InputStream in = PropertyReader.class.getClassLoader().getResourceAsStream(file)) {
            if (in == null) {
                throw new RuntimeException("Config file not found: " + file);
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not read " + file, e);
        }
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Missing property: " + key);
        }
        return value.trim();
    }
}
