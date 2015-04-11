package com.roman.ttu.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.TessClient.R;
import com.roman.ttu.client.Application;

import static com.roman.ttu.client.SharedPreferenceManager.USER_NAME;

public class DashboardActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);
        setContentView(R.layout.activity_dashboard);
        TextView footerTextView = (TextView) findViewById(R.id.footer_textview);
        footerTextView.setText(preferenceManager.getString(USER_NAME));
        initButtons();
    }

    private void initButtons() {
        Button cameraButton = (Button) findViewById(R.id.btn_camera);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ReceiptPictureTakingActivity.class));
                overridePendingTransition(R.animator.activity_fadein, R.animator.activity_fadeout);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
