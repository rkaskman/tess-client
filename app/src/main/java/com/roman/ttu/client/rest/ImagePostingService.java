package com.roman.ttu.client.rest;

import com.roman.ttu.client.rest.model.ImagesWrapper;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface ImagePostingService {
    @POST("/ocr/postImage")
    public void postImages(@Body ImagesWrapper imagesWrapper,
                           Callback<String> callback);

}
