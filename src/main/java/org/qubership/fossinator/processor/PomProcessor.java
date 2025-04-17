package org.qubership.fossinator.processor;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;

import java.nio.file.Path;

@Slf4j
public class PomProcessor extends AbstractProcessor {
    private final static String POM_FILE_NAME = "pom.xml";

    @Override
    public boolean shouldBeExecuted() {
        return !ConfigReader.getConfig().getDependenciesToReplace().isEmpty();
    }

    @Override
    public void process(String dir) {
        processDir(dir, POM_FILE_NAME);
    }

    @Override
    public void processFile(Path filePath) {
        //we should create new instance of PomFileHandler each time, because it has state
        PomFileHandler handler = new PomFileHandler();
        boolean updated = handler.handle(filePath);

        if (updated) {
            updatedFilesNumber.addAndGet(1);
        }
    }
}
