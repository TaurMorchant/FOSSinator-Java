package org.qubership.fossinator.cli;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.processor.Processor;
import picocli.CommandLine;

import java.util.ServiceLoader;

@Slf4j
@CommandLine.Command(name = "transform", description = "Performs automatic migration steps")
class TransformCommand implements Runnable {

    @CommandLine.Option(names = {"-d", "--dir"}, description = "Dir to process")
    private String dir;

    @Override
    public void run() {
        if (dir == null) {
            dir = ".";
        }
        StaticJavaParser.setConfiguration(
                new ParserConfiguration()
                        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        );

        ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
        for (Processor processor : processors) {
            long start = System.currentTimeMillis();
            log.info("----- Execute processor {}. [START]", processor.getClass().getSimpleName());
            processor.process(dir);
            log.info("----- Execute processor {}. [END]. Time spent: {}", processor.getClass().getSimpleName(), System.currentTimeMillis() - start);
        }
    }
}
