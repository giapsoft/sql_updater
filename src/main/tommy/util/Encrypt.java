package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Encrypt {
    public static final String joiner = "]T:M-/:/";
    public static final Gson gson = new GsonBuilder().create();
    public static String encode(Object object) {
        return joiner + gson.toJson(object);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static void saveJson(Object obj, String fullPath) {
        String rawData = toJson(obj);
        FileUt.write(fullPath, rawData);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static String encodeList(List<?> list) {
        return joiner + list.stream().map(gson::toJson).collect(Collectors.joining(joiner));
    }

    public static <T> List<T> decode(String raw, Class<T> clazz) {
        return Arrays.stream(raw.split(joiner)).filter(StringUt::isNotEmptyTrim)
                .map(s -> fromJson(s, clazz))
                .collect(Collectors.toList());
    }

    public static <T> T findJson(Class<T> clazz, String filePath) {
        try {
            String raw = FileUt.read(filePath);
            return fromJson(raw, clazz);

        } catch (Exception ex) {
            FileUt.deleteFile(filePath);
            return null;
        }

    }

    public static <T> List<T> findAll(Class<T> clazz, String filePath) {
        String raw = FileUt.read(filePath);
        return decode(raw, clazz);
    }

}
