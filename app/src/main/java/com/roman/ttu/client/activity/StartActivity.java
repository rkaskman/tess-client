package com.roman.ttu.client.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.roman.ttu.client.R;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.roman.ttu.client.Application;
import com.roman.ttu.client.GoogleTokenRetriever;
import com.roman.ttu.client.rest.SignInService;
import com.roman.ttu.client.model.User;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import java.io.IOException;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.text.TextUtils.isEmpty;
import static com.roman.ttu.client.SharedPreferenceManager.*;

public class StartActivity extends AbstractActivity {
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    public static final int REQUEST_AUTH_CODE = 1888;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    private static final String TAG = "StartActivity";

    //TODO: replace with real for testing
    private String SENDER_ID = "413749891691";

    @Inject
    SignInService signInService;

    private SignInCallBack signInCallBack = new SignInCallBack();
    private GoogleCloudMessaging gcm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);
        setContentView(R.layout.auth_layout);
        initProgressBar();
        authenticateUser(savedInstanceState);
    }

    private void initProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void authenticateUser(Bundle savedInstanceState) {
        if (!preferenceManager.hasPreference(GOOGLE_USER_EMAIL)) {
            String[] accountTypes = new String[]{GOOGLE_ACCOUNT_TYPE};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    accountTypes, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT, savedInstanceState);
        } else {
            verifySession();
        }
    }

    protected void verifySession() {
        try {
            establishSessionIfExpired();
        } catch (Exception e) {
            handleTokenRetrievalError(e);
        }
    }

    protected void establishSessionIfExpired() throws IOException, GoogleAuthException {
        if (!isDeviceOnline()) {
            if(preferenceManager.hasPreference(GOOGLE_USER_EMAIL)) {
//                Intent intent = new Intent();
//                intent.putExtra()
            }
            return;
        } else {
            new GoogleTokenRetriever(this, tokenRetrievalCallback,
                    preferenceManager.getString(GOOGLE_USER_EMAIL)).execute();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                preferenceManager.save(GOOGLE_USER_EMAIL, email);
                verifySession();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
            }
        }
        if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            verifySession();
        }
    }

    private void proceedToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerPlayServiceAndProceedToDashboard() {
        if(checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            String registrationId = getRegistrationId();

            if (isEmpty(registrationId)) {
                registerInBackground();
            } else {
                proceedToDashboard();
            }
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String registrationId = gcm.register(SENDER_ID);
                    preferenceManager.save(GCM_REGISTRATION_ID, registrationId);
                    preferenceManager.save(APP_VERSION, getAppVersion());
                    return registrationId;
                } catch (IOException ex) {
                    Log.e(TAG, ex.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String message) {
                if(message != null) {
                    proceedToDashboard();
                } else {
                    Toast.makeText(StartActivity.this, getString(R.string.error_gcm_reg), Toast.LENGTH_LONG);
                }
            }
        }.execute(null, null, null);
    }

    private String getRegistrationId() {
        String regId = preferenceManager.getString(GCM_REGISTRATION_ID);
        if(isEmpty(regId)) {
            Log.i(TAG, "Registration not found.");
            return null;
        }

        long registeredVersion = preferenceManager.getLong(APP_VERSION, Long.MIN_VALUE);
        long currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return null;
        }
        return regId;
    }

    private int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private GoogleTokenRetriever.OnTokenRetrievedAction tokenRetrievalCallback = new GoogleTokenRetriever.OnTokenRetrievedAction() {
        @Override
        public void onSuccess(String authToken) {
            signInService.signIn(authToken, signInCallBack);
        }

        @Override
        public void onFailure() {
            finishWithError();
        }
    };

    public class SignInCallBack extends AuthenticationAwareActivityCallback<User> {
        @Override
        public void success(User user, Response response) {
            super.success(user, response);
            setResult(Activity.RESULT_OK);
            if(isOnApplicationStartUp()) {
                saveUser(user);
                registerPlayServiceAndProceedToDashboard();
            } else {
                finish();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            finishWithError();
        }
    }

    private void saveUser(User user) {
        preferenceManager.save(USER_NAME, user.name);
        preferenceManager.save(USER_ID, user.userId);
    }

    private void finishWithError() {
        setResult(Activity.RESULT_CANCELED);
        if(isOnApplicationStartUp()) {
            startActivity(new Intent(this, AuthErrorActivity.class));
        }
        finish();
    }

    private boolean isOnApplicationStartUp() {
        return getCallingActivity() == null;
    }

    public void handleTokenRetrievalError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            StartActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }
}