package com.roman.ttu.client.rest;

import com.roman.ttu.client.rest.model.Expense;
import com.roman.ttu.client.rest.model.ExpenseRequest;


import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;

public interface ExpenseService {

    @GET("/expense/forUserAndPeriod")
    public void get(@Body ExpenseRequest expenseRequest, Callback<List<Expense>> callback);
}
