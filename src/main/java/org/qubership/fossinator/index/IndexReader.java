package org.qubership.fossinator.index;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
public class IndexReader {
    public static Index read(String filename) {
        Index result = new Index();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            log.error("Cannot read file as index: {}", filename, e);
            System.exit(1);
        }

        return result;
    }
}
