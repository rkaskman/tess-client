package com.roman.ttu.client.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.roman.ttu.client.Application;
import com.roman.ttu.client.GcmIntentService;
import com.roman.ttu.client.R;
import com.roman.ttu.client.model.RecognitionErrorResponse;
import com.roman.ttu.client.model.RecognizedReceiptData;
import com.roman.ttu.client.rest.ExpenseService;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

import retrofit.RetrofitError;
    import retrofit.client.Response;

    import static com.roman.ttu.client.GcmIntentService.*;


    public class ResponseHandlingActivity extends AuthenticationAwareActivity {

        @Inject
    ExpenseService expenseService;
    private ResponseHandlingActivityCallback callback = new ResponseHandlingActivityCallback();

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

    private void initSuccessfulLayoutElements(final RecognizedReceiptData recognizedReceiptData) {
        View layoutRecognitionSuccessful = findViewById(R.id.layout_recognition_successful);
        layoutRecognitionSuccessful.setVisibility(View.VISIBLE);
        TextView regNumber = (TextView) layoutRecognitionSuccessful.findViewById(R.id.reg_number);
        regNumber.setText(recognizedReceiptData.companyId);

        TextView company = (TextView) layoutRecognitionSuccessful.findViewById(R.id.company);
        company.setText(recognizedReceiptData.companyName);

        TextView totalCost = (TextView) layoutRecognitionSuccessful.findViewById(R.id.total_cost);

        BigDecimal formattedTotalCost = recognizedReceiptData.totalCost.setScale(2, RoundingMode.HALF_UP);
        totalCost.setText(formattedTotalCost.toString() + " " + recognizedReceiptData.currency);

        View buttonsHolder = findViewById(R.id.buttons_success);
        buttonsHolder.setVisibility(View.VISIBLE);

        Button confirm = (Button) buttonsHolder.findViewById(R.id.button_confirm);
        Button decline = (Button) buttonsHolder.findViewById(R.id.button_decline);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseService.confirm(recognizedReceiptData.recognitionId, callback);
            }
        });

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseService.decline(recognizedReceiptData.recognitionId, callback);
            }
        });
    }

    private void initErroneousLayoutElements(RecognitionErrorResponse errorResponse) {
        View layoutRecognitionFailed = findViewById(R.id.layout_recognition_failed);
        layoutRecognitionFailed.setVisibility(View.VISIBLE);
        TextView regNumber = (TextView) layoutRecognitionFailed.findViewById(R.id.recognized_reg_number);
        regNumber.setText(errorResponse.recognizedRegNr);
        TextView totalCost = (TextView) layoutRecognitionFailed.findViewById(R.id.recognized_total_cost);
        totalCost.setText(errorResponse.recognizedTotalCost);

        View buttonsHolder = findViewById(R.id.buttons_error);
        buttonsHolder.setVisibility(View.VISIBLE);

        Button retryTakingPicturesButton = (Button) buttonsHolder.findViewById(R.id.button_retry_taking_pictures);
        Button submitManually = (Button) buttonsHolder.findViewById(R.id.button_submit_manually);

        retryTakingPicturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResponseHandlingActivity.this, ReceiptPictureTakingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        submitManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResponseHandlingActivity.this, ManualExpenseSubmissionActivity.class);
                startActivity(intent);
                finish();
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    public class ResponseHandlingActivityCallback extends AuthenticationAwareActivityCallback {
        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
            progressDialog.dismiss();
            finish();
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.dismiss();
            Toast.makeText(ResponseHandlingActivity.this, error.getMessage(), Toast.LENGTH_LONG);
        }
    };
}