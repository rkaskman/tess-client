package com.roman.ttu.client.rest;

import com.roman.ttu.client.model.ExpenseRequest;
import com.roman.ttu.client.model.ExpenseResponseContainer;


import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

public interface ExpenseService {

    @POST("/expense/forUserAndPeriod")
    void get(@Body ExpenseRequest expenseRequest, Callback<ExpenseResponseContainer> callback);

    @POST("/expense/confirm/{recognitionId}")
    void confirm(@Path("recognitionId") String recognitionId, Callback<Object> callback);

    @POST("/expense/decline/{recognitionId}")
    void decline(@Path("recognitionId") String recognitionId, Callback<Object> callback);
}
