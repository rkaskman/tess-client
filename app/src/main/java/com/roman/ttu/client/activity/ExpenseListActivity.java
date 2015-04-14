package com.roman.ttu.client.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.TessClient.R;
import com.roman.ttu.client.rest.ExpenseService;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;

public class ExpenseListActivity extends AuthenticationAwareActivity {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private Calendar startDate;
    private Calendar endDate;

    private ImageView startDateArrow;
    private ImageView endDateArrow;

    private TextView startDateTextView;
    private TextView endDateTextView;


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

        startDateTextView.setText(DATE_FORMAT.format(startDate));
        endDateTextView.setText(DATE_FORMAT.format(endDate));


        startDateArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment startDatePicker = getStartDatePicker();
                startDatePicker.show(getFragmentManager(), "startDatePicker");
            }
        });

        endDateArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment endDatePicker = getEndDatePicker();
                endDatePicker.show(getFragmentManager(), "endDatePicker");
            }
        });
    }

    private DatePickerFragment getStartDatePicker() {
       return new DatePickerFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                return new DatePickerDialog(getActivity(), this, startDate.get(Calendar.YEAR),
                        startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));
            }

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                startDate.set(Calendar.YEAR, year);
                startDate.set(Calendar.MONTH, monthOfYear);
                startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                startDateTextView.setText(DATE_FORMAT.format(startDate));
            }
        };
    }

    private DatePickerFragment getEndDatePicker() {
        return new DatePickerFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                return new DatePickerDialog(getActivity(), this, endDate.get(Calendar.YEAR),
                        endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH));
            }

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                endDate.set(Calendar.YEAR, year);
                endDate.set(Calendar.MONTH, monthOfYear);
                endDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                endDateTextView.setText(DATE_FORMAT.format(endDate));
            }
        };
    }

    private void initDatePickers() {
        endDate = Calendar.getInstance();
        startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);
    }


    public abstract static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
    }
}