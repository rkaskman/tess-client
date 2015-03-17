package com.roman.ttu.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.TessClient.R;
import com.roman.ttu.client.Application;
import com.roman.ttu.client.RestClient;

import javax.inject.Inject;


public class DashboardActivity extends Activity {

    @Inject
    RestClient restClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);
        setContentView(R.layout.dashboard_layout);
        Button camButton = (Button) findViewById(R.id.btn_camera);

        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
