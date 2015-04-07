package com.roman.ttu.client.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.TessClient.R;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.roman.ttu.client.Application;
import com.roman.ttu.client.GoogleTokenRetriever;
import com.roman.ttu.client.SharedPreferenceManager;
import com.roman.ttu.client.rest.SignInService;
import com.roman.ttu.client.rest.model.User;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import java.io.IOException;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.roman.ttu.client.SharedPreferenceManager.*;
import static com.roman.ttu.client.SharedPreferencesConfig.GOOGLE_USER_EMAIL;

public class StartActivity extends AbstractActivity {
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    public static final int REQUEST_AUTH_CODE = 1888;
    public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;



    @Inject
    SignInService signInService;

    private SignInCallBack signInCallBack = new SignInCallBack();

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
            Toast.makeText(this, "Device is not online", Toast.LENGTH_LONG).show();
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
                proceedToDashboard();
            }
            finish();
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            finishWithError();
        }
    }

    private void saveUser(User user) {
        preferenceManager.save(USER_NAME, user.userName);
        preferenceManager.save(USER_ID, user.userName);
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
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            StartActivity.this,
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
}