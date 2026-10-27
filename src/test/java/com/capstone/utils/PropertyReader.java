package com.capstone.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    private static final Properties props = new Properties();
    private static final String ENV = System.getProperty("env", "qa");

    static {
        String fileName = ENV + ".properties";
        try (InputStream in = PropertyReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new RuntimeException("Config file not found: " + fileName);
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not read " + fileName, e);
        }
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Key not found in " + ENV + ".properties: " + key);
        }
        return value.trim();
    }

    public static String getEnv() {
        return ENV;
    }
}