package com.roman.ttu.client;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.roman.ttu.client.activity.StartActivity;

import java.io.IOException;


public class GoogleTokenRetriever extends AsyncTask<Void, Void, String> {
    private static final String TAG = "AuthTokenRetriever";
    protected StartActivity activity;

    private static final String SCOPES = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    private OnTokenRetrievedAction onTokenRetrievedAction;
    protected String mEmail;

    public GoogleTokenRetriever(StartActivity activity, OnTokenRetrievedAction onTokenRetrievedAction,
                                String email) {
        this.activity = activity;
        this.onTokenRetrievedAction = onTokenRetrievedAction;
        this.mEmail = email;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return GoogleAuthUtil.getToken(activity, mEmail, SCOPES);
        } catch (IOException e) {
            onError("Connection error", e);
        } catch (UserRecoverableAuthException userRecoverableException) {
            activity.handleTokenRetrievalError(userRecoverableException);
        } catch (GoogleAuthException e) {
            onError("Fatal error", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String authToken) {
        if (authToken != null) {
            onTokenRetrievedAction.onSuccess(authToken);
        } else {
            onTokenRetrievedAction.onFailure();
        }
    }

    protected void onError(final String msg, Exception e) {
        if (e != null) {
            Log.e(TAG, "Exception: ", e);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public interface OnTokenRetrievedAction {
        void onSuccess(String authToken);

        void onFailure();
    }
}