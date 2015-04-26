package com.roman.ttu.client.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.roman.ttu.client.R;
import com.roman.ttu.client.adapter.ExpenseAdapter;
import com.roman.ttu.client.rest.ExpenseService;
import com.roman.ttu.client.model.ExpenseRequest;
import com.roman.ttu.client.model.ExpenseResponseContainer;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.animation.Animation.AnimationListener;
import static android.widget.AdapterView.OnItemLongClickListener;
import static com.roman.ttu.client.adapter.ExpenseAdapter.STATE_ACCEPTED;
import static com.roman.ttu.client.model.ExpenseResponseContainer.Expense;

public class ExpenseListActivity extends AuthenticationAwareActivity {

    public static final int ITEM_CLICK_DELAY = 1000;
    private static final int REMOVAL_ANIMATION_DURATION = 200;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    public static final String START_DATE_PICKER = "startDatePicker";
    public static final String END_DATE_PICKER = "endDatePicker";
    public static final int CONFIRM_MENU_ITEM = 0;
    public static final int REMOVE_MENU_ITEM = 1;
    private Calendar startDate;
    private Calendar endDate;

    private View startDateArrow;
    private View endDateArrow;

    private TextView startDateTextView;
    private TextView endDateTextView;
    private ListView expenseListView;
    private View errorLine;
    private TextView errorText;

    private long lastRequestedExpenseId = 0;
    private ExpenseAdapter expenseAdapter;
    private ProgressDialog progressDialog;
    private Menu menu;

    private Button findExpensesButton;
    private ExpensesScrollListener expensesScrollListener = new ExpensesScrollListener();

    private Expense currentlySelectedExpense;

    private View currentlySelectedExpenseView;
    private Drawable initialBackGroundDrawable;

    private Long itemLongClicked;
    @Inject
    ExpenseService expenseService;

    private ExpenseQueryCallback expenseQueryCallback = new ExpenseQueryCallback();
    private ExpenseConfirmationCallback expenseConfirmationCallback = new ExpenseConfirmationCallback();
    private ExpenseDeclineCallback expenseDeclineCallback = new ExpenseDeclineCallback();

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
        startDateArrow = findViewById(R.id.arrow_from_date);
        endDateArrow = findViewById(R.id.arrow_to_date);
        expenseListView = (ListView) findViewById(R.id.expense_list_view);
        findExpensesButton = (Button) findViewById(R.id.find_expenses_button);
        errorLine = findViewById(R.id.error_line);
        errorText = (TextView) findViewById(R.id.error_text);

        startDateTextView.setText(DATE_FORMAT.format(startDate.getTime()));
        endDateTextView.setText(DATE_FORMAT.format(endDate.getTime()));

        createProgressDialog();

        expenseListView.setOnScrollListener(expensesScrollListener);
        expenseListView.setOnItemLongClickListener(itemLongClickListener);
        expenseListView.setOnItemClickListener(itemClickListener);

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
                findExpenses();
            }
        });
    }

    private void findExpenses() {
        if(!validateDate()) {
            setErrorVisible(true);
            return;
        }

        ExpenseRequest request = createExpenseRequest();
        setErrorVisible(false);
        if (expenseAdapter != null) {
            expenseAdapter.clear();
        }
        lastRequestedExpenseId = 0;
        expenseService.get(request, expenseQueryCallback);
        progressDialog.show();
    }

    private boolean validateDate() {
        if(startDate.after(endDate)) {
            errorText.setText(getString(R.string.start_date_after_end));
            return false;
        }

        if(endDate.after(Calendar.getInstance())) {
            errorText.setText(getString(R.string.end_date_cant_be_in_future));
            return false;
        }

        return true;
    }

    private void createProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    private void setErrorVisible(boolean visible) {
        errorLine.setBackgroundColor(getResources().getColor(visible ? R.color.red_error : R.color.dark_gray));
        errorText.setVisibility(visible ? View.VISIBLE : View.GONE);
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


    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (currentlySelectedExpenseView != null && currentlySelectedExpenseView.equals(view)
                    && isOverDelay()) {
                view.setBackground(initialBackGroundDrawable);
                currentlySelectedExpenseView = null;
                currentlySelectedExpense = null;
                setMenuVisible(false);
            }
        }
    };

    private boolean isOverDelay() {
        return itemLongClicked != null && System.currentTimeMillis() - itemLongClicked > ITEM_CLICK_DELAY;
    }

    private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (currentlySelectedExpense != null) {
                return false;
            }

            Expense selectedExpense = expenseAdapter.getExpenses().get(position);
            if (selectedExpense == null || !ExpenseAdapter.STATE_INITIAL.equals(selectedExpense.state)) {
                return false;
            }

            if (initialBackGroundDrawable == null) {
                initialBackGroundDrawable = view.getBackground();
            }

            view.setBackground(getDrawable(R.drawable.dark_grey_color));
            currentlySelectedExpense = selectedExpense;
            currentlySelectedExpenseView = view;
            setMenuVisible(true);
            itemLongClicked = System.currentTimeMillis();
            return false;
        }
    };

    private void setMenuVisible(boolean visible) {
        menu.getItem(0).setVisible(visible);
        menu.getItem(1).setVisible(visible);
    }

    private class ExpensesScrollListener implements AbsListView.OnScrollListener {
        private boolean lastItemReached = true;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            switch (view.getId()) {
                case R.id.expense_list_view:
                    final int lastItem = firstVisibleItem + visibleItemCount;
                    if (lastItem == totalItemCount && !lastItemReached) {
                        lastItemReached = true;
                        ExpenseRequest expenseRequest = createExpenseRequest();
                        expenseRequest.lastId = ExpenseListActivity.this.lastRequestedExpenseId;
                        expenseService.get(expenseRequest, expenseQueryCallback);
                        progressDialog.show();
                    }
            }
        }
    }

    public class ExpenseQueryCallback extends AuthenticationAwareActivityCallback<ExpenseResponseContainer> {
        @Override
        public void success(ExpenseResponseContainer expenseResponseContainer, Response response) {
            super.success(expenseResponseContainer, response);
            progressDialog.dismiss();
            List<Expense> expenses = expenseResponseContainer.expenseList;
            if (!expenses.isEmpty()) {
                if (expenseAdapter == null) {
                    expenseAdapter = new ExpenseAdapter(ExpenseListActivity.this, R.layout.view_expense_item,
                            expenses);
                    expenseListView.setAdapter(expenseAdapter);
                } else {
                    expenseAdapter.addAll(expenses);
                }
                lastRequestedExpenseId = expenses.get(expenses.size() - 1).id;
            }
            expensesScrollListener.lastItemReached = expenseResponseContainer.lastReached;
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.expense_list_menu, this.menu);

        menu.getItem(CONFIRM_MENU_ITEM).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                progressDialog.show();
                expenseService.confirm(String.valueOf(currentlySelectedExpense.id), expenseConfirmationCallback);
                return false;
            }
        });

        menu.getItem(REMOVE_MENU_ITEM).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                progressDialog.show();
                expenseService.decline(String.valueOf(currentlySelectedExpense.id), expenseDeclineCallback);
                return false;
            }
        });

        return true;
    }

    public class ExpenseConfirmationCallback extends AuthenticationAwareActivityCallback {
        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
            progressDialog.dismiss();
            markExpenseConfirmed();
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.dismiss();
            Toast.makeText(ExpenseListActivity.this,
                    "Failed to confirm expense", Toast.LENGTH_LONG).show();
        }
    }

    private void markExpenseConfirmed() {
        currentlySelectedExpense.state = STATE_ACCEPTED;
        expenseAdapter.markConfirmed(currentlySelectedExpenseView);
        currentlySelectedExpenseView.setBackground(initialBackGroundDrawable);
        deselectItemAndHideMenu();
    }

    private void deselectItemAndHideMenu() {
        currentlySelectedExpense = null;
        currentlySelectedExpenseView = null;
        setMenuVisible(false);
    }

    private void removeExpense() {
        AnimationListener animationListener = new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                deselectItemAndHideMenu();
            }
        };
        animateItemRemoval(currentlySelectedExpenseView, animationListener);
    }

    private void animateItemRemoval(final View view, AnimationListener listener) {
        final int height = view.getHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = height - (int) (height * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setAnimationListener(listener);
        animation.setDuration(REMOVAL_ANIMATION_DURATION);
        view.startAnimation(animation);
    }

    public class ExpenseDeclineCallback extends AuthenticationAwareActivityCallback {
        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
            progressDialog.dismiss();
            removeExpense();
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
        }
    }
}