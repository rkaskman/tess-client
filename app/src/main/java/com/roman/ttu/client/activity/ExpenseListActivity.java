package com.roman.ttu.client.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.TessClient.R;
import com.roman.ttu.client.adapter.ExpenseAdapter;
import com.roman.ttu.client.rest.ExpenseService;
import com.roman.ttu.client.rest.model.Expense;
import com.roman.ttu.client.rest.model.ExpenseRequest;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class ExpenseListActivity extends AuthenticationAwareActivity {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    public static final String START_DATE_PICKER = "startDatePicker";
    public static final String END_DATE_PICKER = "endDatePicker";
    private Calendar startDate;
    private Calendar endDate;

    private ImageView startDateArrow;
    private ImageView endDateArrow;

    private TextView startDateTextView;
    private TextView endDateTextView;
    private ListView expenseListView;

    private long lastId = 0;

    private ExpenseAdapter expenseAdapter;

    private Button findExpensesButton;

    @Inject
    ExpenseService expenseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_list);
        initDatePickers();
        initLayoutElements();
    }

    private void initLayoutElements() {
        endDateTextView = (TextView) findViewById(R.id.to_date);
        startDateTextView = (TextView) findViewById(R.id.from_date);
        startDateArrow = (ImageView) findViewById(R.id.arrow_from_date);
        endDateArrow = (ImageView) findViewById(R.id.arrow_to_date);
        expenseListView = (ListView) findViewById(R.id.expense_list_view);
        findExpensesButton = (Button) findViewById(R.id.find_expenses_button);

        startDateTextView.setText(DATE_FORMAT.format(startDate.getTime()));
        endDateTextView.setText(DATE_FORMAT.format(endDate.getTime()));


        startDateArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(startDate, startDateTextView, START_DATE_PICKER);
            }
        });

        endDateArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(endDate, endDateTextView, END_DATE_PICKER);
            }
        });

        startDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(startDate, startDateTextView, START_DATE_PICKER);
            }
        });

        endDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(endDate, endDateTextView, END_DATE_PICKER);
            }
        });

        findExpensesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseAdapter = null;
                ExpenseRequest request = createExpenseRequest();
                expenseService.get(request, new ExpenseQueryCallback());
            }
        });
    }

    private ExpenseRequest createExpenseRequest() {
        ExpenseRequest request = new ExpenseRequest();
        request.startDate = DateUtils.truncate(startDate.getTime(), Calendar.DATE);

        if (DateUtils.isSameDay(endDate.getTime(), new Date())) {
            request.endDate = endDate.getTime();
        } else {
            request.endDate = DateUtils.addMilliseconds(DateUtils.ceiling(endDate.getTime(), Calendar.DATE), -1);
        }

        return request;
    }

    private void showDatePicker(final Calendar cal, final TextView dateText, String tag) {
        DatePickerFragment datePickerFragment = new DatePickerFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                return new DatePickerDialog(getActivity(), this, cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                dateText.setText(DATE_FORMAT.format(cal.getTime()));
            }
        };
        datePickerFragment.show(getFragmentManager(), tag);
    }

    private void initDatePickers() {
        endDate = Calendar.getInstance();
        startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);
    }

    public abstract static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
    }

    class ExpenseQueryCallback extends AuthenticationAwareActivityCallback<List<Expense>> {

        @Override
        public void success(List<Expense> expenses, Response response) {
            super.success(expenses, response);

            if (expenseAdapter == null) {
                expenseAdapter = new ExpenseAdapter(ExpenseListActivity.this, R.layout.view_expense_item, expenses);
                lastId = expenses.get(expenses.size() -1).id;
                expenseListView.setAdapter(expenseAdapter);
            } else {
                expenseAdapter.addAll(expenses);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
        }
    }
}