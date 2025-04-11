package org.qubership.fossinator.config;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigReader {
    private static Config config;

    public static void readConfig() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            config = objectMapper.readValue(new File("config.yaml"), Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setConfig(Config config) {
        ConfigReader.config = config;
    }

    public static Config.JavaConfig getConfig() {
        return config.getJava();
    }
}
