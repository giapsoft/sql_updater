package util;

import org.apache.commons.io.FileUtils;
import setup.Config;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileUt {

    public static void deleteFolder(String folderPath) {
        File file = new File(folderPath);
        try {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else if (file.getParentFile() != null && file.getParentFile().isDirectory()) {
                FileUtils.deleteDirectory(file.getParentFile());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void deleteFile(String filePath) {
        try {
            if (exist(filePath)) {
                FileUtils.delete(new File(filePath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String read(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                return new String(Files.readAllBytes(Paths.get(filePath)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static boolean exist(String path) {
        return new File(path).exists();
    }

    public static void write(String filePath, String content) {
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
        write(filePath, String.join("", read(filePath), append));
    }

    public static void openDir(String fileOrDirPath) throws IOException {
        File file = new File(fileOrDirPath);
        if (file.isDirectory()) {
            Desktop.getDesktop().open(file);
        } else if (file.getParentFile().isDirectory()) {
            Desktop.getDesktop().open(file.getParentFile());
        }
    }

}
