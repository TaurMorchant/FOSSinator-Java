package org.qubership.fossinator.cli;

import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.processor.Processor;
import picocli.CommandLine;

import java.util.ServiceLoader;

@Slf4j
@CommandLine.Command(name = "transform", description = "Performs automatic migration steps")
class TransformCommand extends AbstractCommand {

    @Override
    protected void runCommand() {
        ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
        for (Processor processor : processors) {
            if (processor.shouldBeExecuted()) {
                long start = System.currentTimeMillis();
                log.info("----- Execute processor {}. [ START]", processor.getClass().getSimpleName());
                processor.process(dir);
                log.info("----- Execute processor {}. [FINISH]. Files changed: {}. Time spent: {}\n",
                        processor.getClass().getSimpleName(), processor.getUpdatedFilesNumber(), System.currentTimeMillis() - start);
            } else {
                log.info("----- Processor {} was skipped because its configuration is missing\n", processor.getClass().getSimpleName());
            }
        }
    }
}
