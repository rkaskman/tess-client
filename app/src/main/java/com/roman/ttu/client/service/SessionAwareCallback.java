package com.roman.ttu.client.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Header;
import retrofit.client.Response;

import static android.content.SharedPreferences.Editor;
import static com.roman.ttu.client.SharedPreferencesConfig.PREFERENCE_KEY;
import static com.roman.ttu.client.SharedPreferencesConfig.SESSION_EXPIRES_AT_KEY;

public abstract class SessionAwareCallback<T> implements Callback<T> {


    public SessionAwareCallback(Context context) {
        this.context = context;
    }

    private Context context;


}