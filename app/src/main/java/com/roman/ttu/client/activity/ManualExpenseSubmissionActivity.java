package com.roman.ttu.client.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.roman.ttu.client.R;
import com.roman.ttu.client.model.ExpenseInput;
import com.roman.ttu.client.rest.ExpenseService;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import java.math.BigDecimal;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class ManualExpenseSubmissionActivity extends AuthenticationAwareActivity {

    @Inject
    ExpenseService expenseService;

    private Button submit;
    private EditText regNumberEditText;
    private EditText totalCostEditText;

    private TextView errorTextRegNumber;
    private View errorLineRegNumber;

    private TextView errorTextTotalCost;
    private View errorLineTotalCost;

    private SubmissionCallback submissionCallback = new SubmissionCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_expense_submission);

        regNumberEditText = (EditText) findViewById(R.id.reg_nr_value);
        totalCostEditText = (EditText) findViewById(R.id.total_cost_value);
        errorTextRegNumber = (TextView) findViewById(R.id.error_reg_number);
        errorTextTotalCost = (TextView) findViewById(R.id.error_total_cost);
        errorLineRegNumber = findViewById(R.id.error_line_reg_number);
        errorLineTotalCost = findViewById(R.id.error_line_total_cost);

        submit = (Button) findViewById(R.id.submit_expense_manual_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideErrors();
                if (validateValues()) {
                    progressDialog.show();
                    expenseService.submitManually(createExpense(), submissionCallback);
                }
            }
        });
    }

    private ExpenseInput createExpense() {
        ExpenseInput expenseInput = new ExpenseInput();
        expenseInput.regNumber = errorTextRegNumber.getText().toString();
        expenseInput.totalCost = new BigDecimal(errorTextTotalCost.getText().toString());

        return expenseInput;
    }

    private boolean validateValues() {
        boolean validateRegNumber = validateRegNumber();
        boolean validTotalCost = validateTotalCost();
        return validateRegNumber && validTotalCost;
    }

    private boolean validateRegNumber() {
        String regNumberString = regNumberEditText.getText().toString().trim();

        if (TextUtils.isEmpty(regNumberString)) {
            showRegNumberError("Reg. number can't be empty");
            return false;
        }
        if (!regNumberString.matches("[0-9]{8}")) {
            showRegNumberError("Invalid reg. number");
            return false;
        }
        return true;
    }

    private boolean validateTotalCost() {
        String totalCostString = totalCostEditText.getText().toString().trim();
        if (TextUtils.isEmpty(totalCostString)) {
            showTotalCostError("Total cost can't be empty");
            return false;
        }
        if (!(totalCostString.matches("[0-9]{0,8}(\\.[0-9]{1,2})?") &&
                new BigDecimal(totalCostString).compareTo(BigDecimal.ZERO) > 0)) {
            showTotalCostError("Invalid total cost");
            return false;
        }
        return true;
    }

    private void showTotalCostError(String message) {
        errorTextTotalCost.setText(message);
        errorTextTotalCost.setVisibility(View.VISIBLE);
        errorLineTotalCost.setVisibility(View.VISIBLE);
    }

    private void showRegNumberError(String message) {
        errorTextRegNumber.setText(message);
        errorTextRegNumber.setVisibility(View.VISIBLE);
        errorLineRegNumber.setVisibility(View.VISIBLE);
    }

    private void hideErrors() {
        errorTextRegNumber.setVisibility(View.GONE);
        errorTextTotalCost.setVisibility(View.GONE);
        errorLineRegNumber.setVisibility(View.GONE);
        errorLineTotalCost.setVisibility(View.GONE);
    }

    public class SubmissionCallback extends AuthenticationAwareActivityCallback {
        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
            progressDialog.dismiss();
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.dismiss();


        }
    }
}