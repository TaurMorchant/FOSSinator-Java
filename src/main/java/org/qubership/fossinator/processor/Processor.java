package org.qubership.fossinator.processor;

public interface Processor {
    void process(String dir);

    int getUpdatedFilesNumber();

    boolean shouldBeExecuted();
}
