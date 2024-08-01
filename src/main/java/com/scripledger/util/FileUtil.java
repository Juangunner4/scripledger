package com.scripledger.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtil {

    public static void writeToFile(String path, String data) throws IOException {
        Files.write(Paths.get(path), data.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static String readFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    public static void deleteFile(String path) throws IOException {
        Files.deleteIfExists(Paths.get(path));
    }
}
