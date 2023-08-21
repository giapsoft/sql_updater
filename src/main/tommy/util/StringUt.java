package util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUt {
    static final String noneAlphaDigitUnderscore = "[^a-zA-Z \\d_-]+";
    static final String capital = "(?=[A-Z][a-z]+)";
    static final String alphaDigitSpace = "[A-Za-z0-9 ]+";
    static final String alphaUnderline = "[A-Za-z_]+";

    public static String lower_underline(String text) {
        return Arrays.stream(Arrays.stream(text.split(noneAlphaDigitUnderscore)).map(String::trim).distinct().filter(s -> !s.isEmpty()).collect(Collectors.joining("_"))
                .split(capital)).filter(s -> !StringUt.isEmptyTrim(s)).collect(Collectors.joining("_"))
                .toLowerCase();
    }

    public static boolean isAlphaDigitSpace(String text) {
        String txt  = text == null ? "" : text;
        return txt.matches(alphaDigitSpace);
    }

    public static boolean isAlphaUnderline(String text) {
        String txt  = text == null ? "" : text;
        return txt.matches(alphaUnderline);
    }

    public static String capSpace(String text) {
        return Arrays.stream(lower_underline(text).split("_")).filter(s -> s.length() > 0).map(s -> s.substring(0, 1).toUpperCase() + (s.length() > 1 ? s.substring(1) : ""))
                .collect(Collectors.joining(" "));
    }

    public static boolean isEmptyTrim(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isNotEmptyTrim(String text) {
        return !isEmptyTrim(text);
    }

    public static String emptyTrim(String trim) {
        return trim == null ? "" : trim.trim();
    }

    public static String alphaDigitOnly(String text) {
        return emptyTrim(text).replaceAll("[^A-Za-z0-9]", "");
    }

    public static boolean isSearchContains(String text, String searchKey) {
        return alphaDigitOnly(text).toLowerCase().contains(alphaDigitOnly(searchKey).toLowerCase());
    }
}
