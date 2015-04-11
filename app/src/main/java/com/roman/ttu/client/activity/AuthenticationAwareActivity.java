package com.roman.ttu.client.activity;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;


import static com.roman.ttu.client.SharedPreferencesConfig.GOOGLE_USER_EMAIL;

public abstract class AuthenticationAwareActivity extends AbstractActivity {

    public static final int TIME_BUFFER_BEFORE_SESSION_EXPIRY_MILLIS = 3 * 60 * 1000;
    public static final String SESSION_EXPIRES_AT = "sessionExpiresAt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferenceManager.hasPreference(GOOGLE_USER_EMAIL) && sessionExpired()) {
            if(isDeviceOnline()) {
                Intent intent = new Intent(AuthenticationAwareActivity.this, StartActivity.class);
                startActivityForResult(intent, StartActivity.REQUEST_AUTH_CODE);
            }
        }
    }


    protected boolean sessionExpired() {
        return getSessionRenewalTime() < System.currentTimeMillis();
    }

    protected long getSessionRenewalTime() {
        long sessionExpiresAt = preferenceManager.getLong(SESSION_EXPIRES_AT);
        return sessionExpiresAt - TIME_BUFFER_BEFORE_SESSION_EXPIRY_MILLIS;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == StartActivity.REQUEST_AUTH_CODE && resultCode == Activity.RESULT_CANCELED) {
            finishActivityAndShowAuthError();
        }
    }

    protected void finishActivityAndShowAuthError() {
        startActivity(new Intent(this, AuthErrorActivity.class));
        finish();
    }
}