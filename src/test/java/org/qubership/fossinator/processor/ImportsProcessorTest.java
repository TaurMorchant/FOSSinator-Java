package org.qubership.fossinator.processor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubership.fossinator.config.Config;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.Import;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportsProcessorTest {

    @AfterEach
    void tearDown() {
        ConfigReader.setConfig(null);
    }

    @Test
    void processFile_hasNotImportsToChange() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplace(new ArrayList<>() {{
            add(Import.builder().oldName("test1").newName("test2").build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                package test;
                
                import java.io.*;
                
                public class Main {
                }
                """;

        generalTest(input, input, false);
    }

    @Test
    void processFile_hasImportsToChange_equalName() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplace(new ArrayList<>() {{
            add(Import
                    .builder()
                    .oldName("com.organization.test.FooClass")
                    .newName("org.corporation.test.BarClass")
                    .build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                package test;
                
                import java.io.*;
                import com.organization.test.FooClass;
                
                public class Main {
                }
                """;

        String expected = """
                package test;
                
                import java.io.*;
                import org.corporation.test.BarClass;
                
                public class Main {
                }
                """;

        generalTest(input, expected, true);
    }

    @Test
    void processFile_hasImportsToChange_containsName() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplace(new ArrayList<>() {{
            add(Import
                    .builder()
                    .oldName("com.organization")
                    .newName("org.corporation")
                    .build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                package test;
                
                import java.io.*;
                import com.organization.test.FooClass;
                import com.organization.bar.*;
                
                public class Main {
                }
                """;

        String expected = """
                package test;
                
                import java.io.*;
                import org.corporation.test.FooClass;
                import org.corporation.bar.*;
                
                public class Main {
                }
                """;

        generalTest(input, expected, true);
    }

    private void generalTest(String input, String expected, boolean isUpdateExpected) {
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(input).getResult().get();

        ImportsProcessor processor = new ImportsProcessor();

        boolean updated = processor.processFile(cu);

        String actual = cu.toString();

        assertEquals(isUpdateExpected, updated);
        assertEquals(expected, actual);
    }
}