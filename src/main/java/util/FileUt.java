package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUt {
    public static String readFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return new String(Files.readAllBytes(Paths.get(filePath)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void writeJsonObject(String filePath, Object jsonRaw) {
        writeFile(filePath, Finder.gson.toJson(jsonRaw));
    }

    public static void writeFile(String filePath, String content) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write(content);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendToFile(String filePath, String append) {
        writeFile(filePath, String.join("\\n", readFile(filePath), append));
    }

}
