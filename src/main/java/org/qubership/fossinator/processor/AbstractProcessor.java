package org.qubership.fossinator.processor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Getter
@Slf4j
public abstract class AbstractProcessor implements Processor {
    protected int updatedFilesNumber = 0;

    protected void processDir(String dir, String fileSuffix) {
        Path dirPath = Paths.get(dir);

        try (Stream<Path> s = Files.walk(dirPath)) {
            s.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(fileSuffix))
                    .forEach(this::processFile);
        } catch (IOException e) {
            log.error("Error while processing files in dir {}", dir);
        }
    }

    abstract void processFile(Path file);
}
