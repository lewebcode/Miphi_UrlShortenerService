package miphi.project.util;

import java.io.*;
import java.util.Properties;

public class ConfigService {
    private static final String CONFIG_FILE_PATH = "src/main/resources/properties.config";
    private final Properties properties;

    public ConfigService() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла конфигурации: " + e.getMessage(), e);
        }
    }

    public String getConfigValue(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntConfigValue(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public long getLongConfigValue(String key, long defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Long.parseLong(value) : defaultValue;
    }
}
