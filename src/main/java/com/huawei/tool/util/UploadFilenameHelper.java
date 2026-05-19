package com.huawei.tool.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * multipart 上传文件名的解码与落盘命名（落盘路径仅使用 ASCII，避免 Windows/Java8 路径编码问题）。
 */
public final class UploadFilenameHelper {

    private static final String STORAGE_XLSX = "order.xlsx";
    private static final String STORAGE_XLS = "order.xls";

    private UploadFilenameHelper() {
    }

    /**
     * 修正 multipart 将 UTF-8 文件名按 ISO-8859-1 解析导致的乱码（常见于 Tomcat/Java8）。
     */
    public static String decodeOriginalFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        if (!looksLikeUtf8Mojibake(filename)) {
            return filename;
        }
        String fixed = new String(filename.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        if (isReasonableFilename(fixed)) {
            return fixed;
        }
        return filename;
    }

    /** 落盘文件名（仅 ASCII），与原始展示名解耦。 */
    public static String storageFileName(String decodedOriginal) {
        String lower = decodedOriginal.toLowerCase();
        if (lower.endsWith(".xls") && !lower.endsWith(".xlsx")) {
            return STORAGE_XLS;
        }
        return STORAGE_XLSX;
    }

    public static String displayFileName(String decodedOriginal) {
        return Paths.get(decodedOriginal).getFileName().toString().replace("..", "_");
    }

    public static String taskNameFromFile(String displayFileName) {
        int dot = displayFileName.lastIndexOf('.');
        if (dot > 0) {
            return displayFileName.substring(0, dot);
        }
        return displayFileName;
    }

    private static boolean looksLikeUtf8Mojibake(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 'Ã' || c == 'Â' || c == 'å' || c == 'ä' || c == 'æ' || c == 'é' || c == 'ø' || c == 'ç') {
                return true;
            }
        }
        return false;
    }

    private static boolean isReasonableFilename(String s) {
        return s != null && !s.isEmpty() && !s.contains("\uFFFD") && s.length() <= 255;
    }
}
