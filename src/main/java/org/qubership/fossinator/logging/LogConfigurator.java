package org.qubership.fossinator.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class LogConfigurator {
    public static void configureLogging(boolean verbose) {
        if (verbose) {
            Configurator.setRootLevel(Level.DEBUG);
        } else {
            Configurator.setRootLevel(Level.INFO);
        }
    }
}
