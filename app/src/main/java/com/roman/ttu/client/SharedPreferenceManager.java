package com.roman.ttu.client;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {

    SharedPreferences sharedPreferences;

    public SharedPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SharedPreferencesConfig.PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public String getString(String name) {
        return sharedPreferences.getString(name, null);
    }

    public long getLong(String name) {
        return sharedPreferences.getLong(name, 0L);
    }

    public void save(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void save(String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public boolean hasPreference(String key) {
        return sharedPreferences.contains(key);
    }
}