package org.qubership.fossinator.processor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.ImportPattern;
import org.qubership.fossinator.index.ClassIndex;
import org.qubership.fossinator.index.ClassIndexReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class ImportPatternsProcessor implements Processor {
    @Override
    public void process(String dir) {
        Path dirPath = Paths.get(dir);

        if (ConfigReader.getConfig().getImportsToReplaceByPattern().isEmpty()) {
            return;
        }

        try (Stream<Path> paths = Files.walk(dirPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(this::processFilePath);
        } catch (IOException e) {
            log.error("Error while processing files in dir {}", dir);
        }
    }

    void processFilePath(Path filePath) {
        try {
            CompilationUnit compilationUnit = getCompilationUnit(filePath);

            boolean updated = processFile(compilationUnit);

            if (updated) {
                saveChanges(filePath, compilationUnit);
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

        List<ImportDeclaration> imports = cu.getImports();
        for (int i = 0; i < imports.size(); i++) {
            ImportDeclaration imp = imports.get(i);

            ClassIndex index = ClassIndexReader.getIndex();
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
        }
    }
}
