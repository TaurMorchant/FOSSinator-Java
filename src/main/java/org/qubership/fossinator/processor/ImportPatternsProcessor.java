package org.qubership.fossinator.processor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.ImportPattern;
import org.qubership.fossinator.index.ClassesIndex;
import org.qubership.fossinator.index.Index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class ImportPatternsProcessor extends AbstractProcessor {

    private static final String JAVA_CLASS_EXTENSION = ".java";

    @Override
    public boolean shouldBeExecuted() {
        return !ConfigReader.getConfig().getImportsToReplaceByPattern().isEmpty();
    }

    @Override
    public void process(String dir) {
        processDir(dir, JAVA_CLASS_EXTENSION);
    }

    @Override
    void processFile(Path filePath) {
        try {
            CompilationUnit compilationUnit = getCompilationUnit(filePath);

            boolean updated = processFile(compilationUnit);

            if (updated) {
                saveChanges(filePath, compilationUnit);
                updatedFilesNumber.addAndGet(1);
                log.debug("File was updated: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("Cannot parse file: {}", filePath);
        }
    }

    CompilationUnit getCompilationUnit(Path filePath) throws IOException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(filePath);
        LexicalPreservingPrinter.setup(compilationUnit);

        return compilationUnit;
    }

    boolean processFile(CompilationUnit cu) {
        boolean updated = false;

        Index index = ClassesIndex.getIndex();

        List<ImportDeclaration> imports = cu.getImports();
        for (int i = 0; i < imports.size(); i++) {
            ImportDeclaration imp = imports.get(i);

            ImportPattern matchedPattern = getMatchedPattern(imp);
            if (matchedPattern != null) {
                String importToSearchInIndex = imp.isStatic() ? getClassNameOfStaticImport(imp) : imp.getNameAsString();

                if (index.contains(importToSearchInIndex)) {
                    ImportDeclaration newImport = createNewImport(imp, matchedPattern);
                    imports.set(i, newImport);
                    updated = true;
                }
            }
        }
        return updated;
    }

    String getClassNameOfStaticImport(ImportDeclaration imp) {
        if (!imp.isStatic()) {
            throw new IllegalArgumentException("getClassNameOfStaticImport method can be used only with static imports. Current import: " + imp.getNameAsString());
        }
        if (imp.isAsterisk()) {
            return imp.getNameAsString();
        }

        int lastDotIndex = imp.getNameAsString().lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new RuntimeException("Unexpected static import: " + imp.getNameAsString());
        }

        return imp.getNameAsString().substring(0, lastDotIndex);
    }

    ImportPattern getMatchedPattern(ImportDeclaration imp) {
        for (ImportPattern impPattern : ConfigReader.getConfig().getImportsToReplaceByPattern()) {
            if (imp.getNameAsString().startsWith(impPattern.getOldPackageName())) {
                return impPattern;
            }
        }
        return null;
    }

    ImportDeclaration createNewImport(ImportDeclaration imp, ImportPattern impPattern) {
        String newImportStr = imp.getNameAsString().replace(impPattern.getOldPackageName(), impPattern.getNewPackageName());
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
