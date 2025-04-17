package org.qubership.fossinator.index;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class IndexReader {
    public static Index read(String filename) {
        Index result = new Index();

        try (InputStream is = IndexReader.class.getClassLoader().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

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
