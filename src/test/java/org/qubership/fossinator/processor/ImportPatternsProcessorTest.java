package org.qubership.fossinator.processor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubership.fossinator.config.Config;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.ImportPattern;
import org.qubership.fossinator.index.ClassesIndex;
import org.qubership.fossinator.index.Index;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportPatternsProcessorTest {

    @AfterEach
    void tearDown() {
        ConfigReader.setConfig(null);
        ClassesIndex.setIndex(null);
    }

    @Test
    void getClassNameOfStaticImport_ifNotStaticImport() {
        String input = """
                import java.io.*;
                """;

        ImportDeclaration imp = getFirstImport(input);

        ImportPatternsProcessor processor = new ImportPatternsProcessor();

        assertThrows(
                IllegalArgumentException.class,
                () -> processor.getClassNameOfStaticImport(imp)
        );
    }

    @Test
    void getClassNameOfStaticImport_ifSimpleStaticImport() {
        String input = """
                import static java.time.DayOfWeek.MONDAY;
                """;

        ImportDeclaration imp = getFirstImport(input);

        ImportPatternsProcessor processor = new ImportPatternsProcessor();

        String result = processor.getClassNameOfStaticImport(imp);

        assertEquals("java.time.DayOfWeek", result);
    }

    @Test
    void getClassNameOfStaticImport_ifStaticImportWithAsterisk() {
        String input = """
                import static java.time.DayOfWeek.*;
                """;

        ImportDeclaration imp = getFirstImport(input);

        ImportPatternsProcessor processor = new ImportPatternsProcessor();

        String result = processor.getClassNameOfStaticImport(imp);

        assertEquals("java.time.DayOfWeek", result);
    }

    private ImportDeclaration getFirstImport(String input) {
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(input).getResult().get();

        return cu.getImports().getFirst().get();
    }

    //--------------------------------------------------------------------------

    @Test
    public void processFile_hasNotImportsToChange() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplaceByPattern(new ArrayList<>() {{
            add(ImportPattern.builder().oldPackageName("com.company").newPackageName("org.organization").build());
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
    public void processFile_hasImportsToChange_butClassNotInIndex() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplaceByPattern(new ArrayList<>() {{
            add(ImportPattern.builder().oldPackageName("com.company").newPackageName("org.organization").build());
        }});
        Config config = new Config(javaConfig);

        ConfigReader.setConfig(config);

        String input = """
                package test;
                
                import java.io.*;
                import com.company.package1.Service;
                
                public class Main {
                }
                """;

        generalTest(input, input, false);
    }

    @Test
    public void processFile_hasClassImportToChange_classInIndex() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplaceByPattern(new ArrayList<>() {{
            add(ImportPattern.builder().oldPackageName("com.company").newPackageName("org.organization").build());
        }});
        Config config = new Config(javaConfig);
        ConfigReader.setConfig(config);

        Index index = new Index() {{
            add("com.company.package1.Service");
        }};
        ClassesIndex.setIndex(index);

        String input = """
                package test;
                
                import java.io.*;
                import com.company.package1.Service;
                
                public class Main {
                }
                """;

        String expected = """
                package test;
                
                import java.io.*;
                import org.organization.package1.Service;
                
                public class Main {
                }
                """;

        generalTest(input, expected, true);
    }

    @Test
    public void processFile_hasPackageImportToChange_butPackageNotInIndex() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplaceByPattern(new ArrayList<>() {{
            add(ImportPattern.builder().oldPackageName("com.company").newPackageName("org.organization").build());
        }});
        Config config = new Config(javaConfig);
        ConfigReader.setConfig(config);

        Index index = new Index() {{
            add("com.company.package1");
        }};
        ClassesIndex.setIndex(index);

        String input = """
                package test;
                
                import java.io.*;
                import com.company.package1.*;
                
                public class Main {
                }
                """;

        String expected = """
                package test;
                
                import java.io.*;
                import org.organization.package1.*;
                
                public class Main {
                }
                """;

        generalTest(input, expected, true);
    }

    @Test
    public void processFile_hasPackageImportToChange_packageInIndex() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplaceByPattern(new ArrayList<>() {{
            add(ImportPattern.builder().oldPackageName("com.company").newPackageName("org.organization").build());
        }});
        Config config = new Config(javaConfig);
        ConfigReader.setConfig(config);

        String input = """
                package test;
                
                import java.io.*;
                import com.company.package1.*;
                
                public class Main {
                }
                """;

        generalTest(input, input, false);
    }

    @Test
    public void processFile_hasPackageImportToChange_staticImport() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplaceByPattern(new ArrayList<>() {{
            add(ImportPattern.builder().oldPackageName("com.company").newPackageName("org.organization").build());
        }});
        Config config = new Config(javaConfig);
        ConfigReader.setConfig(config);

        Index index = new Index() {{
            add("com.company.package1.Service");
        }};
        ClassesIndex.setIndex(index);

        String input = """
                package test;
                
                import java.io.*;
                import static com.company.package1.Service.CONST;
                
                public class Main {
                }
                """;

        String expected = """
                package test;
                
                import java.io.*;
                import static org.organization.package1.Service.CONST;
                
                public class Main {
                }
                """;

        generalTest(input, expected, true);
    }

    @Test
    public void processFile_hasPackageImportToChange_staticWildcardImport() {
        Config.JavaConfig javaConfig = new Config.JavaConfig();
        javaConfig.setImportsToReplaceByPattern(new ArrayList<>() {{
            add(ImportPattern.builder().oldPackageName("com.company").newPackageName("org.organization").build());
        }});
        Config config = new Config(javaConfig);
        ConfigReader.setConfig(config);

        Index index = new Index() {{
            add("com.company.package1.Service");
        }};
        ClassesIndex.setIndex(index);

        String input = """
                package test;
                
                import java.io.*;
                import static com.company.package1.Service.*;
                
                public class Main {
                }
                """;

        String expected = """
                package test;
                
                import java.io.*;
                import static org.organization.package1.Service.*;
                
                public class Main {
                }
                """;

        generalTest(input, expected, true);
    }

    private void generalTest(String input, String expected, boolean isUpdateExpected) {
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(input).getResult().get();

        ImportPatternsProcessor processor = new ImportPatternsProcessor();

        boolean updated = processor.processFile(cu);

        String actual = cu.toString();

        assertEquals(isUpdateExpected, updated);
        assertEquals(expected, actual);
    }
}