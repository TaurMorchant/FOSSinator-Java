package org.qubership.fossinator.logging;

public class LogConfigurator {
    private static final String DEFAULT_LOG_LEVEL_PROP = "org.slf4j.simpleLogger.defaultLogLevel";

    public static void configureLogging(boolean verbose) {
        if (verbose) {
            System.setProperty(DEFAULT_LOG_LEVEL_PROP, "debug");
        } else {
            System.setProperty(DEFAULT_LOG_LEVEL_PROP, "info");
        }
    }
}
