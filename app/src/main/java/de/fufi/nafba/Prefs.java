package de.fufi.nafba;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    static SharedPreferences get(Context c) { return c.getSharedPreferences("island", Context.MODE_PRIVATE); }
    static int number(Context c, String key, int fallback) { return get(c).getInt(key, fallback); }
    static boolean flag(Context c, String key, boolean fallback) { return get(c).getBoolean(key, fallback); }
}
