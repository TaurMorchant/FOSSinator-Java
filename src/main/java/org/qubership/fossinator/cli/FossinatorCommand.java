package org.qubership.fossinator.cli;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "fossinator", mixinStandardHelpOptions = true,
        subcommands = {TransformCommand.class, ValidateCommand.class})
public class FossinatorCommand implements Runnable {

    @Override
    public void run() {
        log.error("Please specify one of sub-commands: transform or validate");
    }
}