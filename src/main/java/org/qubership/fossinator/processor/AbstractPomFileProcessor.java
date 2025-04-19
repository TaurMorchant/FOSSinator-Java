package org.qubership.fossinator.processor;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.Constants;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public abstract class AbstractPomFileProcessor extends WalkThroughFilesProcessor {
    @Override
    String getFileSuffix() {
        return Constants.POM_FILE_NAME;
    }

    @Override
    void processFile(Path filePath) {
        try {
            log.debug("Process pom.xml : {}", filePath.toString());

            byte[] pomContent = Files.readAllBytes(filePath);
            String pomXml = new String(pomContent);

            boolean updated = processPom(filePath, pomXml);

            if (updated) {
                updatedFilesNumber.addAndGet(1);
            }
        } catch (Exception e) {
            log.error("Error while processing pom.xml {}", filePath.toString());
            log.debug("Error details: ", e);
        }
    }

    abstract boolean processPom(Path filePath, String pomXml) throws Exception;
}
