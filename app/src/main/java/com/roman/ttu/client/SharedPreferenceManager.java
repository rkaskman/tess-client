package com.roman.ttu.client;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {

    public static final String USER_NAME = "userName";
    public static final String USER_ID = "userId";
    public static final String PREFERENCE_KEY = "tessPref";
    public static final String GOOGLE_USER_EMAIL = "googleUserId";
    public static final String GCM_REGISTRATION_ID = "gcmRegistrationId";
    public static final String APP_VERSION = "appVersion";

    SharedPreferences sharedPreferences;

    public SharedPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public String getString(String name) {
        return sharedPreferences.getString(name, null);
    }

    public long getLong(String name) {
        return sharedPreferences.getLong(name, 0L);
    }

    public long getLong(String name, long defaultValue) {
        return sharedPreferences.getLong(name, defaultValue);
    }

    public int getInt(String name, int defaultValue) {
        return sharedPreferences.getInt(name, defaultValue);
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