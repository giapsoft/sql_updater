package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mapper.Tracker;
import model.DataType;
import service.Config;

import java.util.*;
import java.util.stream.Collectors;

public class Finder {
    public static final Gson gson = new GsonBuilder().create();

    public static void saves(String fullPath, Collection<?> objects) {
        String rawData = gson.toJson(objects.stream().map(gson::toJson).collect(Collectors.toList()));
        FileUt.writeFile(fullPath, rawData);
    }

    public static <M> List<M> findAll(Class<M> clazz, String fullPath) {
        String raw = FileUt.readFile(fullPath);
        List<?> rawList = gson.fromJson(raw, List.class);

        if (rawList != null) {
            return rawList.stream().map(s -> gson.fromJson(s.toString(), clazz)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static <M> M find(Class<M> clazz, String fullPath) {
        try {
            return gson.fromJson(FileUt.readFile(fullPath), clazz);
        } catch (Exception ex) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void save(Object obj, String fullPath) {
        String rawData = gson.toJson(obj);
        FileUt.writeFile(fullPath, rawData);
    }
}
