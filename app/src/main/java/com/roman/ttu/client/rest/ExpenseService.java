package com.roman.ttu.client.rest;

import com.roman.ttu.client.rest.model.Expense;
import com.roman.ttu.client.rest.model.ExpenseRequest;


import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface ExpenseService {

    @POST("/expense/forUserAndPeriod")
    void get(@Body ExpenseRequest expenseRequest, Callback<List<Expense>> callback);
}
