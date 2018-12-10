package io.github.zkhan93.justshare.utils;

public class LogUtil {

    public static void d(String tag, String template, Object... args) {
        android.util.Log.d(tag, String.format(template, args));
    }

    public static void e(String tag, String template, Object... args) {
        android.util.Log.e(tag, String.format(template, args));
    }

    public static void i(String tag, String template, Object... args) {
        android.util.Log.i(tag, String.format(template, args));
    }

}
