package com.roman.ttu.client.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.roman.ttu.client.Application;
import com.roman.ttu.client.GcmIntentService;
import com.roman.ttu.client.R;
import com.roman.ttu.client.model.RecognitionErrorResponse;
import com.roman.ttu.client.model.RecognizedReceiptData;

import static com.roman.ttu.client.GcmIntentService.*;


public class ResponseHandlingActivity extends AuthenticationAwareActivity {

    private Button retryTakingPicturesButton;
    private Button ignoreErrorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);
        setContentView(R.layout.activity_ocr_response_handling);
        initLayoutElements();
    }

    private void initLayoutElements() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_RECOGNITION_ERROR_RESPONSE)) {
            RecognitionErrorResponse errorResponse =
                    (RecognitionErrorResponse) intent.getSerializableExtra(EXTRA_RECOGNITION_ERROR_RESPONSE);
            initErroneousLayoutElements(errorResponse);

        } else if (intent.hasExtra(GcmIntentService.EXTRA_RECOGNIZED_RECEIPT_DATA)) {
            RecognizedReceiptData recognizedReceiptData =
                    (RecognizedReceiptData) intent.getSerializableExtra(EXTRA_RECOGNIZED_RECEIPT_DATA);
            initSuccessfulLayoutElements(recognizedReceiptData);
        }
    }

    private void initSuccessfulLayoutElements(RecognizedReceiptData recognizedReceiptData) {

    }

    private void initErroneousLayoutElements(RecognitionErrorResponse errorResponse) {
        View layoutRecognitionFailed = findViewById(R.id.layout_recognition_failed);
        layoutRecognitionFailed.setVisibility(View.VISIBLE);
        TextView regNum = (TextView) layoutRecognitionFailed.findViewById(R.id.recognized_reg_num);
        regNum.setText(errorResponse.recognizedRegNr);
        TextView totalCost = (TextView) layoutRecognitionFailed.findViewById(R.id.recognized_total_cost);
        totalCost.setText(errorResponse.recognizedTotalCost);

        View buttonsHolder = findViewById(R.id.buttons_error);
        buttonsHolder.setVisibility(View.VISIBLE);

        retryTakingPicturesButton = (Button) buttonsHolder.findViewById(R.id.button_retry_taking_pictures);
        ignoreErrorButton = (Button) buttonsHolder.findViewById(R.id.button_ignore);

        retryTakingPicturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResponseHandlingActivity.this, ReceiptPictureTakingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ignoreErrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }
}
