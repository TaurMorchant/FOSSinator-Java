package org.qubership.fossinator;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import org.qubership.fossinator.processor.DependenciesProcessor;
import picocli.CommandLine;

@CommandLine.Command(name = "transform", mixinStandardHelpOptions = true)
public class TransformCommand implements Runnable {
    @CommandLine.Option(names = "--dir") String dir;

    public void run() {
        System.out.println("Dir is: " + dir);
        if (dir == null) {
            dir = "C:\\git\\Test Resources For Fossinator\\dbaas-old";
        }
        StaticJavaParser.setConfiguration(
                new ParserConfiguration()
                        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        );

//        ImportsProcessor processor = new ImportsProcessor();
//        processor.process(dir);
        DependenciesProcessor processor = new DependenciesProcessor();
        processor.process(dir);
    }

}