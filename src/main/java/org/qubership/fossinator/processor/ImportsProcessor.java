package org.qubership.fossinator.processor;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.model.Import;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.qubership.fossinator.Constants.JAVA_CLASS_EXTENSION;

@Slf4j
public class ImportsProcessor extends WalkThroughFilesProcessor {

    @Override
    public boolean shouldBeExecuted() {
        return !ConfigReader.getConfig().getImportsToReplace().isEmpty();
    }

    @Override
    public String getFileSuffix(){
        return JAVA_CLASS_EXTENSION;
    }

    @Override
    void processFile(Path filePath) {
        try {
            CompilationUnit compilationUnit = getCompilationUnit(filePath);

            boolean updated = processFile(compilationUnit);

            if (updated) {
                saveChanges(filePath, compilationUnit);
                updatedFilesNumber.addAndGet(1);
            }
        } catch (Exception e) {
            log.warn("Cannot parse file: {}", filePath);
            log.debug("Details: ", e);
        }
    }

    //todo duplication
    CompilationUnit getCompilationUnit(Path filePath) throws IOException {
        StaticJavaParser.setConfiguration(
                new ParserConfiguration()
                        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        );
        CompilationUnit compilationUnit = StaticJavaParser.parse(filePath);
        LexicalPreservingPrinter.setup(compilationUnit);

        return compilationUnit;
    }

    boolean processFile(CompilationUnit cu) {
        boolean updated = false;

        List<ImportDeclaration> imports = cu.getImports();
        for (int i = 0; i < imports.size(); i++) {
            ImportDeclaration imp = imports.get(i);

            for (Import impToReplace : ConfigReader.getConfig().getImportsToReplace()) {
                if (imp.getNameAsString().startsWith(impToReplace.getOldName())) {
                    ImportDeclaration newImport = createNewImport(imp, impToReplace);
                    imports.set(i, newImport);
                    updated = true;
                }
            }
        }
        return updated;
    }

    ImportDeclaration createNewImport(ImportDeclaration imp, Import impToReplace) {
        String newImportStr = imp.getNameAsString().replace(impToReplace.getOldName(), impToReplace.getNewName());
        return new ImportDeclaration(newImportStr, imp.isStatic(), imp.isAsterisk());
    }

    void saveChanges(Path filePath, CompilationUnit compilationUnit) {
        try {
            String updatedCode = LexicalPreservingPrinter.print(compilationUnit);
            Files.write(filePath, updatedCode.getBytes());
        } catch (IOException e) {
            log.error("Cannot write file: {}", filePath);
            log.debug("Error details: ", e);
        }
    }
}
