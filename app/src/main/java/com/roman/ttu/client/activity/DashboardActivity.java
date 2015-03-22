package com.roman.ttu.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.TessClient.R;
import com.roman.ttu.client.Application;

public class DashboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);
        setContentView(R.layout.dashboard_layout);
        Button cameraButton = (Button) findViewById(R.id.btn_camera);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ReceiptPictureTakingActivity.class));
                overridePendingTransition(R.animator.activity_fadein, R.animator.activity_fadeout);
            }
        });
    }
}
