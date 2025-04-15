package org.qubership.fossinator.index;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ClassIndexReader {
    @Getter
    private static ClassIndex index;

    public static void readIndex() throws IOException {
        index = new ClassIndex();

        try (BufferedReader reader = new BufferedReader(new FileReader("classesIndex.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                index.add(line);
            }
        }
    }
}
