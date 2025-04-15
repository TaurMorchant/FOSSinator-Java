package org.qubership.fossinator;

import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.index.ClassIndexReader;
import picocli.CommandLine;

public class Main {
    public static void main(String... args) throws Exception {
        ConfigReader.readConfig();
        ClassIndexReader.readIndex();

        int exitCode = new CommandLine(new TransformCommand()).execute(args);
        System.exit(exitCode);
    }
}
