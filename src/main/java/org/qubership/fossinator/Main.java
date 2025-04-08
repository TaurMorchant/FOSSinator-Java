package org.qubership.fossinator;

import org.qubership.fossinator.config.ConfigReader;
import picocli.CommandLine;

public class Main {
    public static void main(String... args) {
        ConfigReader.readConfig();

        int exitCode = new CommandLine(new TransformCommand()).execute(args);
        System.exit(exitCode);
    }
}
