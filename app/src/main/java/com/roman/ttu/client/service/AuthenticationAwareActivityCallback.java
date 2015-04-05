package com.roman.ttu.client.service;

import com.roman.ttu.client.ApplicationHolder;
import com.roman.ttu.client.SharedPreferenceManager;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

import static com.roman.ttu.client.activity.AuthenticationAwareActivity.SESSION_EXPIRES_AT;

public class AuthenticationAwareActivityCallback<T> implements Callback<T> {
    private static final String EXPIRES_AT_HEADER_KEY = "expiresAt";

    @Inject
    SharedPreferenceManager preferenceManager;

    public AuthenticationAwareActivityCallback() {
        ApplicationHolder.get().getObjectGraph().inject(this);
    }

    @Override
    public void success(T t, Response response) {
        updateSessionExpiryTime(response);
    }

    private void updateSessionExpiryTime(Response response) {
        for (Header header : response.getHeaders()) {
            if (EXPIRES_AT_HEADER_KEY.equals(header.getName())) {
                preferenceManager.save(SESSION_EXPIRES_AT, getSessionExpiresAtTime(header));
                break;
            }
        }
    }

    private long getSessionExpiresAtTime(Header header) {
        return System.currentTimeMillis() + Long.parseLong(header.getValue());
    }

    @Override
    public void failure(RetrofitError error) {
        final Throwable e = error.getCause();
//        handleNonAuthenticatedException(e);
    }
}