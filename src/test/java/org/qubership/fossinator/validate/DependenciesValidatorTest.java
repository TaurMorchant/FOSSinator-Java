package org.qubership.fossinator.validate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.index.DependenciesIndex;
import org.qubership.fossinator.index.Index;

import static org.junit.jupiter.api.Assertions.*;

class DependenciesValidatorTest {

    @AfterEach
    void tearDown() {
        ConfigReader.setConfig(null);
        DependenciesIndex.setIndex(null);
    }

    @Test
    void processLine_itIsNotDependencyLine() {
        Index index = new Index(){{
            add("com.fasterxml.jackson.core:jackson-databind");
        }};

        DependenciesIndex.setIndex(index);

        String input = "com.fasterxml.jackson.core:jackson-databind:jar:2.17.2:compile";

        DependenciesValidator validator = new DependenciesValidator();

        String actual = validator.processLine(input);

        assertEquals(input, actual);
    }

    @Test
    void processLine_itIsDependencyLine_containsBlacklistedDependency() {
        Index index = new Index(){{
            add("com.fasterxml.jackson.core:jackson-databind");
        }};

        DependenciesIndex.setIndex(index);

        String input = "[INFO] +- com.fasterxml.jackson.core:jackson-databind:jar:2.17.2:compile";

        String expected = "[INFO] +- com.fasterxml.jackson.core:jackson-databind:jar:2.17.2:compile   ←   ---DEPRECATED---";

        DependenciesValidator validator = new DependenciesValidator();

        String actual = validator.processLine(input);

        assertEquals(expected, actual);
    }

    @Test
    void processLine_itIsDependencyLine_doNotContainsBlacklistedDependency() {
        Index index = new Index(){{
            add("com.fasterxml.jackson.core:jackson-annotations");
        }};

        DependenciesIndex.setIndex(index);

        String input = "[INFO] +- com.fasterxml.jackson.core:jackson-databind:jar:2.17.2:compile";

        DependenciesValidator validator = new DependenciesValidator();

        String actual = validator.processLine(input);

        assertEquals(input, actual);
    }
}