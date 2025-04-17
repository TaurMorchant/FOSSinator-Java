package org.qubership.fossinator;

import org.qubership.fossinator.cli.FossinatorCommand;
import picocli.CommandLine;

public class Main {
    public static void main(String... args) {
        int exitCode = new CommandLine(new FossinatorCommand()).execute(args);
        System.exit(exitCode);
    }
}
