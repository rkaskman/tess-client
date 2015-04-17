package com.roman.ttu.client.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.TessClient.R;
import com.roman.ttu.client.rest.model.ExpenseResponseContainer;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

public class ExpenseAdapter extends ArrayAdapter<ExpenseResponseContainer.Expense> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private final List<ExpenseResponseContainer.Expense> expenses;
    private final int resource;
    private final LayoutInflater layoutInflater;

    public ExpenseAdapter(Context context, int resource, List<ExpenseResponseContainer.Expense> expenses) {
        super(context, resource, expenses);
        this.layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.expenses = expenses;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View expenseView, ViewGroup parent) {
        if (expenseView != null) {
            return expenseView;
        }

        ExpenseResponseContainer.Expense expense = expenses.get(position);
        expenseView = layoutInflater.inflate(resource, null);

        TextView expenseDate = (TextView) expenseView.findViewById(R.id.expense_date);
        expenseDate.setText(DATE_FORMAT.format(expense.date));

        TextView expenseEnterprise = (TextView) expenseView.findViewById(R.id.expense_enterprise);
        expenseEnterprise.setText(expense.companyName);

        TextView expenseSum = (TextView) expenseView.findViewById(R.id.expense_sum);
        expenseSum.setText(expense.sum);

        return expenseView;
    }

    @Override
    public void add(ExpenseResponseContainer.Expense expense) {
        expenses.add(expense);
        notifyDataSetChanged();
    }

    @Override
    public void addAll(Collection<? extends ExpenseResponseContainer.Expense> expenses) {
        this.expenses.addAll(expenses);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        this.expenses.clear();
        notifyDataSetChanged();
    }
}