package com.simplejava;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class ApplicationConfiguration {

    private final Map<String, Object> config;

    public ApplicationConfiguration(String externalConfigPath) {
        Map<String, Object> defaultConfig = loadClasspathConfig();
        Map<String, Object> externalConfig = externalConfigPath == null ? Map.of() : loadFileConfig(externalConfigPath);
        this.config = merge(defaultConfig, externalConfig);
    }

    public int getParam(String path) {
        Object current = getValue(path);

        if (current instanceof Number number) {
            return number.intValue();
        }

        throw new RuntimeException("Config property is not numeric: " + path);
    }

    public String getStringParam(String path) {
        return (String) getValue(path);
    }

    public boolean getBooleanParam(String path) {
        return (Boolean) getValue(path);
    }

    public Map<String, String> getStringMapParam(String path) {
        Map<?, ?> values = (Map<?, ?>) getValue(path);
        Map<String, String> result = new LinkedHashMap<>();
        values.forEach((key, value) -> result.put((String) key, (String) value));
        return result;
    }

    private Object getValue(String path) {
        Object current = config;

        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                throw new RuntimeException("Missing config property: " + path);
            }
            current = map.get(part);
            if (current == null) {
                throw new RuntimeException("Missing config property: " + path);
            }
        }
        return current;
    }

    private Map<String, Object> loadClasspathConfig() {
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (input == null) {
                return Map.of();
            }
            Yaml yaml = new Yaml();
            Map<String, Object> loaded = yaml.load(input);
            return loaded == null ? Map.of() : loaded;
        } catch (IOException e) {
            throw new RuntimeException("Could not read application.yml", e);
        }
    }

    private Map<String, Object> loadFileConfig(String externalConfigPath) {
        try (var input = Files.newInputStream(Path.of(externalConfigPath))) {
            Yaml yaml = new Yaml();
            Map<String, Object> loaded = yaml.load(input);
            return loaded == null ? Map.of() : loaded;
        } catch (IOException e) {
            throw new RuntimeException("Could not read external config file: " + externalConfigPath, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> merge(Map<String, Object> base, Map<String, Object> override) {
        Map<String, Object> merged = new LinkedHashMap<>(base);

        for (Map.Entry<String, Object> entry : override.entrySet()) {
            Object baseValue = merged.get(entry.getKey());
            Object overrideValue = entry.getValue();

            if (baseValue instanceof Map && overrideValue instanceof Map) {
                merged.put(entry.getKey(), merge((Map<String, Object>) baseValue, (Map<String, Object>) overrideValue));
            } else {
                merged.put(entry.getKey(), overrideValue);
            }
        }

        return merged;
    }
}
