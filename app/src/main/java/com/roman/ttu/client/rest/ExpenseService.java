package com.roman.ttu.client.rest;

import com.roman.ttu.client.rest.model.ExpenseRequest;
import com.roman.ttu.client.rest.model.ExpenseResponseContainer;


import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface ExpenseService {

    @POST("/expense/forUserAndPeriod")
    void get(@Body ExpenseRequest expenseRequest, Callback<ExpenseResponseContainer> callback);
}
