package org.qubership.fossinator.processor;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;

import java.nio.file.Path;

import static org.qubership.fossinator.Constants.POM_FILE_NAME;

@Slf4j
public class ReplaceDependenciesPomProcessor extends AbstractPomFileProcessor {

    @Override
    public boolean shouldBeExecuted() {
        return !ConfigReader.getConfig().getDependenciesToReplace().isEmpty();
    }

    @Override
    public String getFileSuffix(){
        return POM_FILE_NAME;
    }

    @Override
    boolean processPom(Path filePath, String pomXml) throws Exception {
        //we should create new instance of PomFileHandler each time, because it has state
        ReplaceDependenciesPomFileHandler handler = new ReplaceDependenciesPomFileHandler();
        return handler.handle(filePath, pomXml);
    }
}
