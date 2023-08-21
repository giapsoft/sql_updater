package util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Cache {
    private Map<String, Object> cacheMap = new HashMap<>();
    public <T> T cache(String name, Supplier<T> getter) {
        return (T) cacheMap.computeIfAbsent(name, n -> getter.get());
    }
}
