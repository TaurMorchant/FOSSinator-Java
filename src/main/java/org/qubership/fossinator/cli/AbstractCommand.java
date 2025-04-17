package org.qubership.fossinator.cli;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.index.ClassesIndex;
import org.qubership.fossinator.index.DependenciesIndex;
import org.qubership.fossinator.index.IndexReader;
import org.qubership.fossinator.logging.LogConfigurator;
import picocli.CommandLine.Option;

@Slf4j
public abstract class AbstractCommand implements Runnable {
    @Option(names = {"-d", "--dir"}, description = "Dir to process", defaultValue = ".")
    protected String dir;

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose (debug) output")
    protected boolean verbose;

    @Override
    public void run() {
        LogConfigurator.configureLogging(verbose);
        if (verbose) {
            log.debug("Verbose mode activated. Additional logs will be printed");
        }

        LoadConfigs();

        log.info("Running {} command in directory: {}", this.getClass().getSimpleName(), dir);
        runCommand();
    }

    protected abstract void runCommand();

    private static void LoadConfigs() {
        ConfigReader.readConfig();
        ClassesIndex.setIndex(IndexReader.read("classesIndex.txt"));
        DependenciesIndex.setIndex(IndexReader.read("dependenciesBlackList.txt"));
    }
}
