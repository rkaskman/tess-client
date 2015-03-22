package com.roman.ttu.client.rest;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;

public interface ImagePostingService {
    @POST("/ocr/postImage")
    public void postImages(@Body ImagesWrapper imagesWrapper,
                           Callback<String> callback);

}
