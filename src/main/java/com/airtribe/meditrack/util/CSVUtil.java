package com.airtribe.meditrack.util;

import com.airtribe.meditrack.exception.DataPersistenceException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class CSVUtil {

    private CSVUtil() {}

    public static List<String[]> read(String filePath) throws DataPersistenceException {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    records.add(line.split(","));
                }
            }
        } catch (IOException e) {
            throw new DataPersistenceException("read", "Failed to read file: " + filePath, e);
        }
        return records;
    }

    public static void write(String filePath, List<String[]> records) throws DataPersistenceException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String[] record : records) {
                writer.write(String.join(",", record));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new DataPersistenceException("write", "Failed to write file: " + filePath, e);
        }
    }
}