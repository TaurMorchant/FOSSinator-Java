package org.qubership.fossinator;

import org.qubership.fossinator.cli.FossinatorCommand;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.index.ClassesIndex;
import org.qubership.fossinator.index.DependenciesIndex;
import org.qubership.fossinator.index.IndexReader;
import picocli.CommandLine;

public class Main {
    public static void main(String... args) {
        ConfigReader.readConfig();
        ClassesIndex.setIndex(IndexReader.read("classesIndex.txt"));
        DependenciesIndex.setIndex(IndexReader.read("dependenciesBlackList.txt"));

        int exitCode = new CommandLine(new FossinatorCommand()).execute(args);
        System.exit(exitCode);
    }
}
