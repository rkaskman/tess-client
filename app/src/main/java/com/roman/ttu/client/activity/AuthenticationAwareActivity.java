package com.roman.ttu.client.activity;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.roman.ttu.client.R;
import com.roman.ttu.client.SharedPreferenceManager;


public abstract class AuthenticationAwareActivity extends AbstractActivity {

    public static final int TIME_BUFFER_BEFORE_SESSION_EXPIRY_MILLIS = 3 * 60 * 1000;
    public static final String SESSION_EXPIRES_AT = "sessionExpiresAt";
    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferenceManager.hasPreference(SharedPreferenceManager.GOOGLE_USER_EMAIL) && sessionExpired()) {
            if(isDeviceOnline()) {
                Intent intent = new Intent(AuthenticationAwareActivity.this, StartActivity.class);
                startActivityForResult(intent, StartActivity.REQUEST_AUTH_CODE);
            } else {

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

    private void createProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_progress));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
}