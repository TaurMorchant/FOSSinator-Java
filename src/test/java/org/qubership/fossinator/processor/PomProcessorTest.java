package org.qubership.fossinator.processor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubership.fossinator.config.Config;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.Dependency;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PomProcessorTest {

    @AfterEach
    void tearDown() {
        ConfigReader.setConfig(null);
    }

    @Test
    void processPom_hasNotDependenciesToChange() throws Exception {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setDependenciesToReplace(new ArrayList<>() {{
            add(Dependency.builder().build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>1.7.6</version>
                        </dependency>
                   </dependencies>
                </project>
                """;

        generalTest(input, input);
    }

    @Test
    void processPom_hasDependenciesToChange() throws Exception {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setDependenciesToReplace(new ArrayList<>() {{
            add(Dependency.builder()
                    .oldGroupId("org.foo")
                    .oldArtifactId("artifact1")
                    .newGroupId("org.bar")
                    .newArtifactId("artifact2")
                    .newVersion("6.6.6")
                    .build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencies>
                        <dependency>
                            <groupId>org.foo</groupId>
                            <artifactId>artifact1</artifactId>
                            <version>1.1.1</version>
                        </dependency>
                   </dependencies>
                </project>
                """;

        String expected = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencies>
                        <dependency>
                            <groupId>org.bar</groupId>
                            <artifactId>artifact2</artifactId>
                            <version>6.6.6</version>
                        </dependency>
                   </dependencies>
                </project>
                """;

        generalTest(input, expected);
    }

    @Test
    void processPom_hasDependenciesToChange_withoutVersion() throws Exception {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setDependenciesToReplace(new ArrayList<>() {{
            add(Dependency.builder()
                    .oldGroupId("org.foo")
                    .oldArtifactId("artifact1")
                    .newGroupId("org.bar")
                    .newArtifactId("artifact2")
                    .newVersion("6.6.6")
                    .build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencies>
                        <dependency>
                            <groupId>org.foo</groupId>
                            <artifactId>artifact1</artifactId>
                        </dependency>
                   </dependencies>
                </project>
                """;

        String expected = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencies>
                        <dependency>
                            <groupId>org.bar</groupId>
                            <artifactId>artifact2</artifactId>
                        </dependency>
                   </dependencies>
                </project>
                """;

        generalTest(input, expected);
    }

    @Test
    void processPom_hasDependenciesToChange_severalDependencies() throws Exception {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setDependenciesToReplace(new ArrayList<>() {{
            add(Dependency.builder()
                    .oldGroupId("org.foo1")
                    .oldArtifactId("artifact1")
                    .newGroupId("org.bar1")
                    .newArtifactId("artifact2")
                    .newVersion("6.6.6")
                    .build());
            add(Dependency.builder()
                    .oldGroupId("org.foo2")
                    .oldArtifactId("artifact3")
                    .newGroupId("org.bar2")
                    .newArtifactId("artifact4")
                    .newVersion("7.7.7")
                    .build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencies>
                        <dependency>
                            <groupId>org.foo1</groupId>
                            <artifactId>artifact1</artifactId>
                            <version>1.1.1</version>
                        </dependency>
                        <dependency>
                            <groupId>org.foo2</groupId>
                            <artifactId>artifact3</artifactId>
                            <version>2.2.2</version>
                        </dependency>
                   </dependencies>
                </project>
                """;

        String expected = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencies>
                        <dependency>
                            <groupId>org.bar1</groupId>
                            <artifactId>artifact2</artifactId>
                            <version>6.6.6</version>
                        </dependency>
                        <dependency>
                            <groupId>org.bar2</groupId>
                            <artifactId>artifact4</artifactId>
                            <version>7.7.7</version>
                        </dependency>
                   </dependencies>
                </project>
                """;

        generalTest(input, expected);
    }

    @Test
    void processPom_hasDependencyManagementToChange() throws Exception {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setDependenciesToReplace(new ArrayList<>() {{
            add(Dependency.builder()
                    .oldGroupId("org.foo1")
                    .oldArtifactId("artifact1")
                    .newGroupId("org.bar1")
                    .newArtifactId("artifact2")
                    .newVersion("6.6.6")
                    .build());
            add(Dependency.builder()
                    .oldGroupId("org.foo2")
                    .oldArtifactId("artifact3")
                    .newGroupId("org.bar2")
                    .newArtifactId("artifact4")
                    .newVersion("7.7.7")
                    .build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.foo1</groupId>
                                <artifactId>artifact1</artifactId>
                                <version>1.1.1</version>
                            </dependency>
                            <dependency>
                                <groupId>org.foo2</groupId>
                                <artifactId>artifact3</artifactId>
                                <version>2.2.2</version>
                            </dependency>
                       </dependencies>
                    </dependencyManagement>
                </project>
                """;

        String expected = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.bar1</groupId>
                                <artifactId>artifact2</artifactId>
                                <version>6.6.6</version>
                            </dependency>
                            <dependency>
                                <groupId>org.bar2</groupId>
                                <artifactId>artifact4</artifactId>
                                <version>7.7.7</version>
                            </dependency>
                       </dependencies>
                    </dependencyManagement>
                </project>
                """;

        generalTest(input, expected);
    }

    private void generalTest(String input, String expected) throws Exception {

        PomProcessor processor = new PomProcessor();

        String actual = processor.processPom(input);

        assertEquals(expected, actual);
    }
}