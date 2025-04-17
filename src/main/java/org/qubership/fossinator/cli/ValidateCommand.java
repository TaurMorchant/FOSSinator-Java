package org.qubership.fossinator.cli;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.validate.DependenciesValidator;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "validate", description = "Checks the project for usage of deprecated libraries")
class ValidateCommand extends AbstractCommand {

    @Override
    protected void runCommand() {
        try {
            DependenciesValidator dependenciesValidator = new DependenciesValidator();
            dependenciesValidator.validateDependencies(dir);
        } catch (Exception e) {
            log.error("Error during project validation", e);
        }
    }
}
