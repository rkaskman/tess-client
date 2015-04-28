package com.roman.ttu.client.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.roman.ttu.client.ApplicationHolder;
import com.roman.ttu.client.R;
import com.roman.ttu.client.model.ExpenseInput;
import com.roman.ttu.client.model.ExpenseResponseContainer;
import com.roman.ttu.client.rest.ExpenseService;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;
import com.roman.ttu.client.util.Util;

import java.math.BigDecimal;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.roman.ttu.client.model.ExpenseResponseContainer.*;

public class ManualExpenseSubmissionActivity extends AuthenticationAwareActivity {

    @Inject
    ExpenseService expenseService;


    private View submissionStage;
    private View confirmationStage;

    private Button submitButton;
    private EditText regNumberEditText;
    private EditText totalCostEditText;

    private TextView errorTextRegNumber;
    private View errorLineRegNumber;

    private TextView errorTextTotalCost;
    private View errorLineTotalCost;

    private Button confirmButton;
    private Button declineButton;

    enum Phase {
        SUBMISSION, CONFIRMATION;
    }

    private SubmissionCallback submissionCallback = new SubmissionCallback();
    private ResponseHandlingActivityCallback responseHandlingActivityCallback = new ResponseHandlingActivityCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ApplicationHolder.get().getObjectGraph().inject(this);

        setContentView(R.layout.activity_manual_expense_submission);

        submissionStage = findViewById(R.id.submission_stage);
        confirmationStage = findViewById(R.id.confirmation_stage);

        initPhase(Phase.SUBMISSION);

        regNumberEditText = (EditText) findViewById(R.id.reg_nr_value);
        totalCostEditText = (EditText) findViewById(R.id.total_cost_value);
        errorTextRegNumber = (TextView) findViewById(R.id.error_reg_number);
        errorTextTotalCost = (TextView) findViewById(R.id.error_total_cost);
        errorLineRegNumber = findViewById(R.id.error_line_reg_number);
        errorLineTotalCost = findViewById(R.id.error_line_total_cost);

        submitButton = (Button) findViewById(R.id.submit_expense_manual_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideErrors();
                if (validateValues()) {
                    progressDialog.show();
                    expenseService.submitManually(createExpense(), submissionCallback);
                }
            }
        });

        confirmButton = (Button) findViewById(R.id.button_confirm);
        declineButton = (Button) findViewById(R.id.button_decline);
    }

    private ExpenseInput createExpense() {
        ExpenseInput expenseInput = new ExpenseInput();
        expenseInput.regNumber = regNumberEditText.getText().toString();
        expenseInput.totalCost = new BigDecimal(totalCostEditText.getText().toString());

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

    private void initPhase(Phase phase) {
        submissionStage.setVisibility(phase == Phase.SUBMISSION ? View.VISIBLE : View.GONE);
        confirmationStage.setVisibility(phase == Phase.CONFIRMATION ? View.VISIBLE : View.GONE);
    }

    private void setExpenseToConfirmValues(Expense expense) {
        TextView totalCost = (TextView) findViewById(R.id.total_cost);
        TextView company = (TextView) findViewById(R.id.company);
        TextView regNumber = (TextView) findViewById(R.id.reg_number);

        totalCost.setText(Util.format(expense.sum).toString() + " " + expense.currency);
        company.setText(expense.companyName);
        regNumber.setText(expense.regNumber);
    }

    public class SubmissionCallback extends AuthenticationAwareActivityCallback<ExpenseResponseContainer.Expense> {
        @Override
        public void success(final Expense expense, Response response) {
            super.success(expense, response);
            progressDialog.dismiss();
            initPhase(Phase.CONFIRMATION);
            setExpenseToConfirmValues(expense);

            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expenseService.confirm(String.valueOf(expense.id), responseHandlingActivityCallback);
                }
            });

            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   expenseService.decline(String.valueOf(expense.id), responseHandlingActivityCallback);
                }
            });
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.dismiss();
            if(error.getMessage().startsWith("404")) {
                showRegNumberError("Invalid reg. number");
                regNumberEditText.requestFocus();
            } else {
                Toast.makeText(ManualExpenseSubmissionActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
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
            Toast.makeText(ManualExpenseSubmissionActivity.this, error.getMessage(), Toast.LENGTH_LONG);
        }
    };
}