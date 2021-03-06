package com.roman.ttu.client.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ExpenseResponseContainer {
    public List<Expense> expenseList;
    public BigDecimal totalSum;
    public boolean lastReached;

    public static class Expense {
        public long id;
        public String companyName;
        public String sum;
        public String currency;
        public String regNumber;
        public Date date;
        public String state;
    }
}
