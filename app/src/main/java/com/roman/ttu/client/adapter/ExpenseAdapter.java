package com.roman.ttu.client.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.roman.ttu.client.R;
import com.roman.ttu.client.model.ExpenseResponseContainer;
import com.roman.ttu.client.util.Util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.roman.ttu.client.model.ExpenseResponseContainer.Expense;

public class ExpenseAdapter extends ArrayAdapter<Expense> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    public static final String STATE_INITIAL = "I";
    public static final String STATE_ACCEPTED = "A";

    private final List<Expense> expenses;
    private final int resource;
    private final LayoutInflater layoutInflater;

    public ExpenseAdapter(Context context, int resource, List<Expense> expenses) {
        super(context, resource, expenses);
        this.layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.expenses = expenses;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View expenseView, ViewGroup parent) {
//        if (expenseView != null) {
//            return expenseView;
//        }

        Expense expense = expenses.get(position);
        expenseView = layoutInflater.inflate(resource, null);

        TextView expenseDate = (TextView) expenseView.findViewById(R.id.expense_date);
        expenseDate.setText(DATE_FORMAT.format(expense.date));

        TextView expenseEnterprise = (TextView) expenseView.findViewById(R.id.expense_enterprise);
        expenseEnterprise.setText(expense.companyName);

        TextView expenseSum = (TextView) expenseView.findViewById(R.id.expense_sum);
        expenseSum.setText(Util.format(expense.sum).toString()
                + " " + expense.currency);

        if (STATE_INITIAL.equals(expense.state)) {
            initializeConfirmation(expenseView);
        }

        return expenseView;
    }

    private void initializeConfirmation(View expenseView) {
        expenseView.findViewById(R.id.confirmation_info_icon).setVisibility(View.VISIBLE);
        expenseView.findViewById(R.id.confirm_notification).setVisibility(View.VISIBLE);
    }

    public void markConfirmed(View expenseView) {
        expenseView.findViewById(R.id.confirmation_info_icon).setVisibility(View.GONE);
        expenseView.findViewById(R.id.confirm_notification).setVisibility(View.GONE);
    }

    public List<Expense> getExpenses() {
        return expenses != null ? Collections.unmodifiableList(expenses) : Collections.<Expense>emptyList();
    }

    @Override
    public void add(Expense expense) {
        expenses.add(expense);
        notifyDataSetChanged();
    }

    @Override
    public void addAll(Collection<? extends Expense> expenses) {
        this.expenses.addAll(expenses);
        notifyDataSetChanged();
    }

    public void removeWithoutNotification(Expense expense) {
        setNotifyOnChange(false);
        this.expenses.remove(expense);
        setNotifyOnChange(true);
    }

    @Override
    public void clear() {
        this.expenses.clear();
        notifyDataSetChanged();
    }
}