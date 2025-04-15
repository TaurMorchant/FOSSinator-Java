package org.qubership.fossinator.config;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigReader {
    private static Config config;

    public static void readConfig() {
        long start = System.currentTimeMillis();
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            config = objectMapper.readValue(new File("config.yaml"), Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
