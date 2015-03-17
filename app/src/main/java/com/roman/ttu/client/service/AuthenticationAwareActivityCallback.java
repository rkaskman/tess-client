package com.roman.ttu.client.service;

import android.app.Activity;
import android.content.SharedPreferences;


import com.roman.ttu.client.activity.AuthenticationAwareActivity;

import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

import static com.roman.ttu.client.SharedPreferencesConfig.PREFERENCE_KEY;
import static com.roman.ttu.client.SharedPreferencesConfig.SESSION_EXPIRES_AT_KEY;

public class AuthenticationAwareActivityCallback<T> extends SessionAwareCallback<T> {
    private Activity activity;
    private static final String EXPIRES_AT_HEADER_KEY = "expiresAt";

    public AuthenticationAwareActivityCallback(AuthenticationAwareActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public void success(T t, Response response) {
        updateSessionExpiryTime(response);
    }

    private void updateSessionExpiryTime(Response response) {
        for (Header header : response.getHeaders()) {
            if (EXPIRES_AT_HEADER_KEY.equals(header.getName())) {
                writeExpiryValueIntoSharedPreferences(header);
                break;
            }
        }
    }

    private void writeExpiryValueIntoSharedPreferences(Header header) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCE_KEY, 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putLong(SESSION_EXPIRES_AT_KEY, Long.parseLong(header.getValue()));
        edit.commit();
    }

    @Override
    public void failure(RetrofitError error) {
        final Throwable e = error.getCause();
        handleNonAuthenticatedException(e);
    }

    private void handleNonAuthenticatedException(Throwable e) {
        //TODO: start Login activity for result
    }
}