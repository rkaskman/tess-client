package com.roman.ttu.client.model;

import java.util.Date;
import java.util.List;

public class ExpenseResponseContainer {
    public List<Expense> expenseList;
    public boolean lastReached;

    public static class Expense {
        public long id;
        public String companyName;
        public String sum;
        public String currency;
        public Date date;
        public String state;
    }
}
