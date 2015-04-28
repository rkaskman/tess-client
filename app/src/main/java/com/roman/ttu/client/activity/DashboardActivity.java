package com.roman.ttu.client.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.roman.ttu.client.R;
import com.roman.ttu.client.Application;

import static com.roman.ttu.client.SharedPreferenceManager.USER_NAME;
import static com.roman.ttu.client.activity.StartActivity.NO_CONNECTION;

public class DashboardActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);
        setContentView(R.layout.activity_dashboard);
        initFooter();
        initButtons();
        checkIfUserWasConnected();
    }

    private void initFooter() {
        TextView footerTextView = (TextView) findViewById(R.id.footer_textview);
        footerTextView.setText(preferenceManager.getString(USER_NAME));
    }

    private void initButtons() {
        Button cameraButton = (Button) findViewById(R.id.btn_camera_recognition);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ReceiptPictureTakingActivity.class));
                overridePendingTransition(R.animator.activity_fadein, R.animator.activity_fadeout);
            }
        });

        Button listExpensesButton = (Button) findViewById(R.id.btn_list_expenses);
        listExpensesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ExpenseListActivity.class));
                overridePendingTransition(R.animator.activity_fadein, R.animator.activity_fadeout);
            }
        });

        Button pendingImages = (Button) findViewById(R.id.btn_pending_images);
        pendingImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, PendingImagesActivity.class));
                overridePendingTransition(R.animator.activity_fadein, R.animator.activity_fadeout);
            }
        });

        Button insertExpenseManually = (Button) findViewById(R.id.insert_expense_manually);
        insertExpenseManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ManualExpenseSubmissionActivity.class));
                overridePendingTransition(R.animator.activity_fadein, R.animator.activity_fadeout);
            }
        });
    }

    private void checkIfUserWasConnected() {
        Intent intent = getIntent();
        if(intent.hasExtra(NO_CONNECTION)) {
            Toast.makeText(this, getString(R.string.no_internet_dashboard), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
