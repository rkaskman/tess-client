package com.roman.ttu.client.rest;

import com.roman.ttu.client.model.ExpenseRequest;
import com.roman.ttu.client.model.ExpenseResponseContainer;


import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface ExpenseService {

    @POST("/expense/forUserAndPeriod")
    void get(@Body ExpenseRequest expenseRequest, Callback<ExpenseResponseContainer> callback);
}
