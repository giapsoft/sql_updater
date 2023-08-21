package util;

import java.util.Arrays;

public class Validator {
    public static String validateTableName(String name) {
        String text = StringUt.emptyTrim(name).toLowerCase();
        if (text.isEmpty()) {
            return "at least 1 [alpha] char";
        }
        if (!StringUt.isAlphaUnderline(text)) {
            return name + ": accept [alpha, underline] only";
        }

        if (!text.matches("[a-z]+[a-z_]*")) {
            return name + ": must start with [alpha]";
        }
        return "";
    }

    public static boolean isValid(Boolean... results) {
        return Arrays.stream(results).allMatch(r -> r);
    }
}
