package org.qubership.fossinator;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.processor.Processor;
import picocli.CommandLine;

import java.util.ServiceLoader;

@Slf4j
@CommandLine.Command(name = "transform", mixinStandardHelpOptions = true)
public class TransformCommand implements Runnable {
    @CommandLine.Option(names = "--dir") String dir;

    public void run() {
        if (dir == null) {
            dir = "C:\\git\\Test Resources For Fossinator\\dbaas-old";
        }
        StaticJavaParser.setConfiguration(
                new ParserConfiguration()
                        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        );

        ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
        for (Processor processor : processors) {
            log.info("----- Execute processor {}. [START]", processor.getClass().getSimpleName());
            processor.process(dir);
            log.info("----- Execute processor {}. [END]", processor.getClass().getSimpleName());
        }
    }

}