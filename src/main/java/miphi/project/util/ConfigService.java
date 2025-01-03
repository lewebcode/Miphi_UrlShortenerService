package miphi.project.util;

import java.io.*;
import java.util.Properties;

/**
 * Сервис для загрузки конфигурационных данных из файла.
 */
public class ConfigService {
    private static final String CONFIG_FILE_PATH = "src/main/resources/properties.config";
    private final Properties properties;

    /**
     * Конструктор, загружающий конфигурационный файл.
     * Файл загружается в объект {@code Properties} для последующего получения значений.
     * При возникновении ошибки загрузки выбрасывается {@code RuntimeException}.
     */
    public ConfigService() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла конфигурации: " + e.getMessage(), e);
        }
    }

    /**
     * Получает строковое значение из конфигурации.
     * Если ключ не найден, возвращается заданное значение по умолчанию.
     *
     * @param key Ключ, соответствующий нужному значению.
     * @param defaultValue Значение по умолчанию, которое будет возвращено, если ключ не найден.
     * @return Значение, ассоциированное с ключом, или значение по умолчанию.
     */
    public String getConfigValue(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Получает целочисленное значение из конфигурации.
     * Если ключ не найден, возвращается заданное значение по умолчанию.
     *
     * @param key Ключ, соответствующий нужному целочисленному значению.
     * @param defaultValue Значение по умолчанию, которое будет возвращено, если ключ не найден.
     * @return Целочисленное значение, ассоциированное с ключом, или значение по умолчанию.
     */
    public int getIntConfigValue(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Получает длинное значение из конфигурации.
     * Если ключ не найден, возвращается заданное значение по умолчанию.
     *
     * @param key Ключ, соответствующий нужному длинному значению.
     * @param defaultValue Значение по умолчанию, которое будет возвращено, если ключ не найден.
     * @return Длинное значение, ассоциированное с ключом, или значение по умолчанию.
     */
    public long getLongConfigValue(String key, long defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Long.parseLong(value) : defaultValue;
    }
}
