package com.roman.ttu.client.service;

import retrofit.Callback;
import retrofit.http.POST;

public interface TestService {

    @POST("/uu")
    public void brah(Callback<String> callback);

}
