package org.qubership.fossinator.processor;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
public abstract class WalkThroughFilesProcessor implements Processor {
    protected AtomicInteger updatedFilesNumber = new AtomicInteger(0);

    @Override
    public void process(String dir) {
        Path dirPath = Paths.get(dir);

        try (Stream<Path> s = Files.walk(dirPath)) {
            s.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(getFileSuffix()))
                    .parallel()
                    .forEach(this::processFile);
        } catch (IOException e) {
            log.error("Error while processing files in dir {}", dir);
            log.debug("Error details: ", e);
        }
    }

    @Override
    public int getUpdatedFilesNumber() {
        return updatedFilesNumber.get();
    }

    abstract String getFileSuffix();

    abstract void processFile(Path file);
}
