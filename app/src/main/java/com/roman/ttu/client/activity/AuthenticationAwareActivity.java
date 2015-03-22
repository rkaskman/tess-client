package com.roman.ttu.client.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.roman.ttu.client.Application;
import com.roman.ttu.client.GoogleTokenRetriever;
import com.roman.ttu.client.rest.RestClient;
import com.roman.ttu.client.SharedPreferencesConfig;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import java.io.IOException;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.roman.ttu.client.GoogleTokenRetriever.OnTokenRetrievedAction;
import static com.roman.ttu.client.SharedPreferencesConfig.GOOGLE_USER_EMAIL;

public abstract class AuthenticationAwareActivity extends Activity {

    public static final int TIME_BUFFER_BEFORE_SESSION_EXPIRY_MILLIS = 3 * 60 * 1000;

    public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

    protected SharedPreferences sharedPreferences;

    @Inject
    RestClient restClient;
    private SignInCallBack signInCallback = new SignInCallBack();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);

        createProgressDialog();
        sharedPreferences = getSharedPreferences(SharedPreferencesConfig.PREFERENCE_KEY, MODE_PRIVATE);
        if (sharedPreferences.contains(GOOGLE_USER_EMAIL)) {
            verifySession();
        }
    }

    private void createProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Authenticating..");
        }
    }

    protected void verifySession() {
        try {
            establishSessionIfExpired();
        } catch (Exception e) {
            handleTokenRetrievalError(e);
        }
    }

    protected boolean establishSessionIfExpired() throws IOException, GoogleAuthException {
        long sessionRenewalTime = getSessionRenewalTime();
        if (sessionRenewalTime < System.currentTimeMillis()) {
            if (!isDeviceOnline()) {
                Toast.makeText(this, "Device is not online", Toast.LENGTH_LONG).show();
                return false;
            }

            progressDialog.show();
            new GoogleTokenRetriever(this, tokenRetrievalCallback,
                    sharedPreferences.getString(GOOGLE_USER_EMAIL, null)).execute();
            return true;
        }
        return false;
    }

    protected long getSessionRenewalTime() {
        long sessionExpiresAt = sharedPreferences.getLong(SharedPreferencesConfig.SESSION_EXPIRES_AT_KEY, 0);
        return sessionExpiresAt - TIME_BUFFER_BEFORE_SESSION_EXPIRY_MILLIS;
    }

    public boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            verifySession();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void handleTokenRetrievalError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            AuthenticationAwareActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    class SignInCallBack extends AuthenticationAwareActivityCallback {
        public SignInCallBack() {
            super(AuthenticationAwareActivity.this);
        }

        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
            progressDialog.dismiss();
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.dismiss();
        }
    }

    protected SignInCallBack getSignInCallback() {
        return signInCallback;
    }

    private OnTokenRetrievedAction tokenRetrievalCallback = new OnTokenRetrievedAction() {
        @Override
        public void onSuccess(String authToken) {
            restClient.getSignInService().signIn(authToken, getSignInCallback());
        }

        @Override
        public void onFailure() {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    };
}