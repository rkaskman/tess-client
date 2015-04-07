package com.roman.ttu.client.rest;

import com.roman.ttu.client.rest.model.User;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Query;

public interface SignInService {
    @POST("/")
    public void signIn(@Query("authToken") String token, Callback<User> callback);
}
