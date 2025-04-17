package org.qubership.fossinator.cli;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.validate.DependenciesValidator;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "validate", description = "Checks the project for usage of deprecated libraries")
class ValidateCommand implements Runnable {

    @CommandLine.Option(names = {"-d", "--dir"}, description = "Dir to process")
    private String dir;

    @Override
    public void run() {
        if (dir == null) {
            dir = ".";
        }
        try {
            DependenciesValidator dependenciesValidator = new DependenciesValidator();
            dependenciesValidator.validateDependencies(dir);
        } catch (Exception e) {
            log.error("Error during project validation", e);
        }
    }
}
