package com.roman.ttu.client.activity;

import android.os.Bundle;

import com.roman.ttu.client.Application;

public class ResponseHandlingActivity extends AuthenticationAwareActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);
    }
}
