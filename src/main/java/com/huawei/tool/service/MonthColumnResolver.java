package com.huawei.tool.service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将「6月」「06月」「6」等规范为两位月份数字，用于选择 psp_mXX_cost / std_mXX_cost 列。
 */
public final class MonthColumnResolver {

    private static final Pattern DIGITS = Pattern.compile("(\\d{1,2})");

    private MonthColumnResolver() {
    }

    /**
     * @return 两位月份，如 "06"；无法解析时返回 null
     */
    public static String normalizeMonthKey(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        Matcher m = DIGITS.matcher(s);
        if (!m.find()) {
            return null;
        }
        int month = Integer.parseInt(m.group(1));
        if (month < 1 || month > 12) {
            return null;
        }
        return String.format(Locale.ROOT, "%02d", month);
    }

    public static String pspColumn(String monthKey) {
        return "psp_m" + monthKey + "_cost";
    }

    public static String stdColumn(String monthKey) {
        return "std_m" + monthKey + "_cost";
    }
}
