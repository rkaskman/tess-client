package com.roman.ttu.client;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.TessClient.R;

public class SubmitOcrActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_send);

        TextView view = (TextView) findViewById(R.id.textView);
        String totalSum = getIntent().getStringExtra("TOTAL_SUM");
        view.setText("Total sum is: "+totalSum);
    }
}
