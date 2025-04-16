package org.qubership.fossinator.validate;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.index.DependenciesIndex;

import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DependenciesValidator {
    private static final String DEPENDENCY_REGEX = """
            ^(\\[INFO]\\s+)?    # [INFO] or nothing
            [|+\\s\\\\]+          # part of tree
            -\\s+               # minus sign befor dependency
            ([\\w\\.-]+)        # groupId
            :
            ([\\w\\.-]+)        # artifactId
            :
            [\\w\\.-]+          # packaging type
            :
            ([\\w\\.-]+)        # version
            .*
""";

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile(DEPENDENCY_REGEX, Pattern.COMMENTS);

    public void validateDependencies(String dir) throws Exception {
        long startTime = System.currentTimeMillis();

        File projectDir = new File(dir);

        Process process = startMvnProcess(projectDir);

        File outputFile = new File(projectDir, "dependency-tree-checked.txt");

        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {


            String line;
            while ((line = reader.readLine()) != null) {
                String annotatedLine = processLine(line);

                if (!updated && !Objects.equals(annotatedLine, line)) {
                    updated = true;
                }

                writer.write(annotatedLine);
                writer.newLine();
            }
        }

        process.waitFor();

        if (updated) {
            log.error("Forbidden dependencies detected. Please, check file: {}. Forbidden dependencies marked by '---DEPRECATED---' label", outputFile.getAbsolutePath());
        } else {
            log.info("No forbidden dependencies found.");
        }

        log.debug("Time spent: {}", System.currentTimeMillis() - startTime);
    }

    Process startMvnProcess(File projectDir) throws IOException {
        String mavenCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
        ProcessBuilder pb = new ProcessBuilder(mavenCmd, "dependency:tree", "-DoutputType=text");
        pb.redirectErrorStream(true);
        pb.directory(projectDir);
        return pb.start();
    }

    String processLine(String line) {
        String result = line;
        Matcher matcher = DEPENDENCY_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            String groupId = matcher.group(2);
            String artifactId = matcher.group(3);
            String dependencyKey = groupId + ":" + artifactId;

            if (DependenciesIndex.getIndex().contains(dependencyKey)) {
                result += "   ←   ---DEPRECATED---";

                log.debug("Forbidden dependency found: {}", dependencyKey);
            }
        }

        return result;
    }
}


