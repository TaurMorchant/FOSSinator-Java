package org.qubership.fossinator.processor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubership.fossinator.config.Config;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.model.Dependency;
import org.qubership.fossinator.config.model.DependencyToAdd;
import org.qubership.fossinator.config.model.DependencyToReplace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AddDependenciesPomProcessorTest {

    @AfterEach
    void tearDown() {
        ConfigReader.setConfig(null);
    }

    @Test
    void processPom_hasDependencyToReplace() throws Exception {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setDependenciesToAdd(new ArrayList<>() {{
            add(DependencyToAdd.builder()
                    .ifDependencyExists(
                            Dependency.builder()
                                    .groupId("org.slf4j")
                                    .artifactId("slf4j-api")
                                    .build()
                    )
                    .addDependency(
                            Dependency.builder()
                                    .groupId("org.apache.commons")
                                    .artifactId("commons-lang3")
                                    .version("3.14.0")
                                    .scope("provided")
                                    .build()
                    ).build()
            );
        }});
        Config config = new Config(javaConfig);
        ConfigReader.setConfig(config);

        String input = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                \t<dependencies>
                \t\t<dependency>
                \t\t\t<groupId>org.slf4j</groupId>
                \t\t\t<artifactId>slf4j-api</artifactId>
                \t\t\t<version>1.7.6</version>
                \t\t</dependency>
                \t</dependencies>
                </project>
                """;

        String expected = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                \t<dependencies>
                \t\t<dependency>
                \t\t\t<groupId>org.slf4j</groupId>
                \t\t\t<artifactId>slf4j-api</artifactId>
                \t\t\t<version>1.7.6</version>
                \t\t</dependency>
                \t\t<dependency>
                \t\t\t<groupId>org.apache.commons</groupId>
                \t\t\t<artifactId>commons-lang3</artifactId>
                \t\t\t<version>3.14.0</version>
                \t\t\t<scope>provided</scope>
                \t\t</dependency>
                \t</dependencies>
                </project>
                """;

        Path filePath = Files.createTempFile("pom", "xml");

        AddDependenciesPomProcessor processor = new AddDependenciesPomProcessor();

        boolean updated = processor.processPom(filePath, input);

        String actual = Files.readString(filePath);

        assertTrue(updated);
        assertEquals(expected, actual);
    }

    @Test
    void processPom_hasSeveralDependenciesToReplace() throws Exception {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setDependenciesToAdd(new ArrayList<>() {{
            add(DependencyToAdd.builder()
                    .ifDependencyExists(
                            Dependency.builder()
                                    .groupId("org.package1")
                                    .artifactId("matched-artifact1")
                                    .build()
                    )
                    .addDependency(
                            Dependency.builder()
                                    .groupId("com.company1")
                                    .artifactId("added-artifact1")
                                    .build()
                    ).build()
            );
            add(DependencyToAdd.builder()
                    .ifDependencyExists(
                            Dependency.builder()
                                    .groupId("org.package3")
                                    .artifactId("matched-artifact3")
                                    .build()
                    )
                    .addDependency(
                            Dependency.builder()
                                    .groupId("com.company3")
                                    .artifactId("added-artifact3")
                                    .build()
                    ).build()
            );
        }});
        Config config = new Config(javaConfig);
        ConfigReader.setConfig(config);

        String input = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                \t<dependencies>
                \t\t<dependency>
                \t\t\t<groupId>org.package1</groupId>
                \t\t\t<artifactId>matched-artifact1</artifactId>
                \t\t</dependency>
                \t\t<dependency>
                \t\t\t<groupId>org.package2</groupId>
                \t\t\t<artifactId>not-matched-artifact2</artifactId>
                \t\t</dependency>
                \t\t<dependency>
                \t\t\t<groupId>org.package3</groupId>
                \t\t\t<artifactId>matched-artifact3</artifactId>
                \t\t</dependency>
                \t</dependencies>
                </project>
                """;

        String expected = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                \t<dependencies>
                \t\t<dependency>
                \t\t\t<groupId>org.package1</groupId>
                \t\t\t<artifactId>matched-artifact1</artifactId>
                \t\t</dependency>
                \t\t<dependency>
                \t\t\t<groupId>com.company1</groupId>
                \t\t\t<artifactId>added-artifact1</artifactId>
                \t\t</dependency>
                \t\t<dependency>
                \t\t\t<groupId>org.package2</groupId>
                \t\t\t<artifactId>not-matched-artifact2</artifactId>
                \t\t</dependency>
                \t\t<dependency>
                \t\t\t<groupId>org.package3</groupId>
                \t\t\t<artifactId>matched-artifact3</artifactId>
                \t\t</dependency>
                \t\t<dependency>
                \t\t\t<groupId>com.company3</groupId>
                \t\t\t<artifactId>added-artifact3</artifactId>
                \t\t</dependency>
                \t</dependencies>
                </project>
                """;

        Path filePath = Files.createTempFile("pom", "xml");

        AddDependenciesPomProcessor processor = new AddDependenciesPomProcessor();

        boolean updated = processor.processPom(filePath, input);

        String actual = Files.readString(filePath);

        assertTrue(updated);
        assertEquals(expected, actual);
    }
}