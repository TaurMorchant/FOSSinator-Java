package org.qubership.fossinator.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigReaderTest {

    @Test
    void readConfig() {
        ConfigReader.readConfig();

        assertNotNull(ConfigReader.getConfig());
    }
}