package org.hv.pocket.utils;

/**
 * @author wujianchuan
 */
public class UnderlineHumpTranslator {
    private static final String UNDER_LINE = "_";

    public static String underlineToHump(String value) {
        StringBuilder result = new StringBuilder();
        String[] segment = value.split(UNDER_LINE);
        for (String s : segment) {
            if (!value.contains("_")) {
                result.append(s);
                continue;
            }
            if (result.length() == 0) {
                result.append(s.toLowerCase());
            } else {
                result.append(s.substring(0, 1).toUpperCase());
                result.append(s.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    public static String humpToUnderline(String value) {
        StringBuilder stringBuilder = new StringBuilder(value);
        int temp = 0;
        if (!value.contains(UNDER_LINE)) {
            for (int index = 0; index < value.length(); index++) {
                if (Character.isUpperCase(value.charAt(index))) {
                    stringBuilder.insert(index + temp, UNDER_LINE);
                    temp += 1;
                }
            }
        }
        return stringBuilder.toString().toUpperCase();
    }
}
