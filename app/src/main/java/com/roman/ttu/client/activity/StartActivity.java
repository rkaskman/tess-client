package com.roman.ttu.client.activity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.AccountPicker;

import java.io.IOException;

import retrofit.client.Response;

import static android.content.SharedPreferences.Editor;
import static com.roman.ttu.client.SharedPreferencesConfig.GOOGLE_USER_EMAIL;

public class StartActivity extends AuthenticationAwareActivity {
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private StartingSignInCallBack startingSignInCallBack = new StartingSignInCallBack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView();
        if (!sharedPreferences.contains(GOOGLE_USER_EMAIL)) {
            String[] accountTypes = new String[]{GOOGLE_ACCOUNT_TYPE};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    accountTypes, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT, savedInstanceState);
        }
    }

    @Override
    protected boolean establishSessionIfExpired() throws IOException, GoogleAuthException {
        if (!super.establishSessionIfExpired()) {
            startDashboardActivity();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                persistAccountChoice(email);
                verifySession();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void persistAccountChoice(String email) {
        Editor editor = sharedPreferences.edit();
        editor.putString(GOOGLE_USER_EMAIL, email);
        editor.commit();
    }

    private void startDashboardActivity() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected SignInCallBack getSignInCallback() {
        return startingSignInCallBack;
    }

    class StartingSignInCallBack extends SignInCallBack {
        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
            startDashboardActivity();
        }
    }
}