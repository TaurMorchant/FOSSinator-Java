package org.qubership.fossinator.processor;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class LombokConfigProcessor extends WalkThroughFilesProcessor{
    private static final String OLD_VALUE = "com.netcracker.cloud.core.log.LoggerWrapperFactory.getLogger";
    private static final String NEW_VALUE = "org.qubership.cloud.core.log.LoggerWrapperFactory.getLogger";

    @Override
    String getFileSuffix() {
        return "lombok.config";
    }

    @Override
    void processFile(Path filePath) {
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            if (content.contains(OLD_VALUE)) {
                String updatedContent = content.replace(OLD_VALUE, NEW_VALUE);
                Files.writeString(filePath, updatedContent, StandardCharsets.UTF_8);
                log.debug("File was updated: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Cannot change file: {}", filePath);
            log.debug("Details: ", e);
        }
    }

    @Override
    public boolean shouldBeExecuted() {
        return true;
    }
}
