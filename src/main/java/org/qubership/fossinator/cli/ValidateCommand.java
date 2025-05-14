package org.qubership.fossinator.cli;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.validate.DependenciesValidator;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "validate", description = "Checks the project for usage of deprecated libraries")
class ValidateCommand extends AbstractCommand {
    @CommandLine.Option(names = {"-b", "--branch"}, description = "Branch name", defaultValue = "")
    protected String branch;

    @Override
    protected void runCommand() {
        try {
            log.info("----- Validate dependencies. [ START]");
            DependenciesValidator dependenciesValidator = new DependenciesValidator();
            dependenciesValidator.validateDependencies(dir, branch);
            log.info("----- Validate dependencies. [FINISH]");
        } catch (Exception e) {
            log.error("Error during project validation");
            log.debug("Error details: ", e);
        }
    }
}
