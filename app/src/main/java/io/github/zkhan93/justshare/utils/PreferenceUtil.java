package io.github.zkhan93.justshare.utils;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.zkhan93.justshare.models.FileMeta;

public class PreferenceUtil {
    public static void saveMap(@NonNull SharedPreferences sharedPreferences, String key, @NonNull
            Map<String, String> map) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> keys = map.keySet();
        editor.putStringSet(key + "_allkeys", keys);
        for (Map.Entry<String, String> me : map.entrySet()) {
            editor.putString(key + "_" + me.getKey(), me.getValue());
        }
        editor.apply();
    }

    public static Map<String, String> readMap(@NonNull SharedPreferences sharedPreferences,
                                              String key) {
        Map<String, String> map = new HashMap<>();
        Set<String> allKeys = sharedPreferences.getStringSet(key + "_allkeys", null);
        if (allKeys == null)
            return null;
        String savedKey;
        for (String mapKey : allKeys) {
            savedKey = key + "_" + mapKey;
            map.put(mapKey, sharedPreferences.getString(savedKey, null));
        }
        return map;
    }

    public static void saveMetaList(@NonNull SharedPreferences sharedPreferences, String key,
                                List<FileMeta> items) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key + "_size", items.size());
        String _key;
        FileMeta fm;
        for (int i = 0; i < items.size(); i++) {
            fm = items.get(i);
            _key = key + "_" + i;
            editor.putString(_key + "_name", fm.name);
            editor.putString(_key + "_mimeType", fm.mimeType);
            editor.putLong(_key + "_length", fm.length);
            editor.putString(_key + "_uri", fm.uri);
        }
        editor.apply();
    }

    public static List<FileMeta> getMetaList(@NonNull SharedPreferences sharedPreferences, String
            key) {
        List<FileMeta> fileMetas = new ArrayList<>();
        int size = sharedPreferences.getInt(key + "_size", 0);
        String _key;
        FileMeta fm;
        for (int i = 0; i < size; i++) {
            _key = key + "_" + i;
            fm = new FileMeta();
            fm.name = sharedPreferences.getString(_key + "_name", null);
            fm.mimeType = sharedPreferences.getString(_key + "_mimeType", null);
            fm.length = sharedPreferences.getLong(_key + "_length", 0);
            fm.uri = sharedPreferences.getString(_key + "_uri", null);
            fileMetas.add(fm);
        }
        return fileMetas;
    }
}
