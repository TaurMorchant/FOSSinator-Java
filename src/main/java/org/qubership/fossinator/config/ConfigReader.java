package org.qubership.fossinator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class ConfigReader {
    private static Config config;

    public static void readConfig() {
        long start = System.currentTimeMillis();
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream("config.yaml")) {
            config = objectMapper.readValue(is, Config.class);
        } catch (IOException e) {
            log.error("Could not read config", e);
            System.exit(1);
        }
        log.debug("readConfig. Execution time = {}", System.currentTimeMillis() - start);
    }

    public static void setConfig(Config config) {
        ConfigReader.config = config;
    }

    public static Config.JavaConfig getConfig() {
        return config.getJava();
    }
}
