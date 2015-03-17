package com.roman.ttu.client.service;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Query;

public interface SignInService {
    @POST("/")
    public void signIn(@Query("authToken") String token, Callback<String> callback);
}
