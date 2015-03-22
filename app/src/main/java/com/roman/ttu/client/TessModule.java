package com.roman.ttu.client;


import com.roman.ttu.client.activity.DashboardActivity;
import com.roman.ttu.client.activity.ReceiptPictureTakingActivity;
import com.roman.ttu.client.activity.SignInActivity;
import com.roman.ttu.client.activity.StartActivity;
import com.roman.ttu.client.rest.RestClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true,
        injects = {                DashboardActivity.class,
                StartActivity.class,
                ReceiptPictureTakingActivity.class})
public class TessModule {
    @Provides
    @Singleton
    RestClient provideRestClient() {
        return new RestClient();
    }
}
