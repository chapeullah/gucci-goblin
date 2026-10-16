package com.chapeullah.guccigoblin.telegram;

public final class TelegramUtils {

    private TelegramUtils() {}

    public static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}
