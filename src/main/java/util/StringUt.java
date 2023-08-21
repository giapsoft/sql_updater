package util;

import java.util.*;
import java.util.stream.Collectors;

public class StringUt {
    public static String emptyTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static String lowerTrim(String value) {
        return emptyTrim(value).toLowerCase();
    }

    public static boolean areEqual(String a, String b) {
        return emptyTrim(a).equals(emptyTrim(b));
    }

    public static String toId(Map<String, String> pk) {
        List<String> keys = new ArrayList<>(pk.keySet());
        keys.sort(Comparator.comparing(s -> s));
        return keys.stream().map(pk::get).collect(Collectors.joining("|"));
    }

    @SafeVarargs
    public static boolean anyContainsFirst(final String item, final Collection<String>... collections) {
        String _item = item.trim().toLowerCase();
        for(Collection<String> collection: collections) {
            if (collection.contains(_item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(String name) {
        return emptyTrim(name).isEmpty();
    }
}
