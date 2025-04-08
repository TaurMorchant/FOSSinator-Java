package org.qubership.fossinator.processor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.Import;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class ImportsProcessor implements Processor {

    public void process(String dir) {
        Path dirPath = Paths.get(dir);

        try (Stream<Path> paths = Files.walk(dirPath)) {
            List<Path> javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();

            for (Path javaFile : javaFiles) {
                processFilePath(javaFile);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processFilePath(Path filePath) throws IOException {
        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(filePath);
            LexicalPreservingPrinter.setup(cu);
        } catch (Exception e) {
            System.err.println("Cannot parse file: " + filePath);
            return;
        }

        boolean updated = processFile(cu);

        if (updated) {
            String updatedCode = LexicalPreservingPrinter.print(cu);
            Files.write(filePath, updatedCode.getBytes());
            System.out.println("Updated: " + filePath);
        }
    }

    private static boolean processFile(CompilationUnit cu) {
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

    private static ImportDeclaration createNewImport(ImportDeclaration imp, Import impToReplace) {
        String newImportStr = imp.getNameAsString().replace(impToReplace.getOldName(), impToReplace.getNewName());
        return new ImportDeclaration(newImportStr, imp.isStatic(), imp.isAsterisk());
    }
}
