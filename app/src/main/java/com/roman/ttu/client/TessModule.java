package com.roman.ttu.client;


import com.roman.ttu.client.activity.AuthenticationAwareActivity;
import com.roman.ttu.client.activity.DashboardActivity;
import com.roman.ttu.client.activity.SignInActivity;
import com.roman.ttu.client.activity.StartActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true,
        injects = {SignInActivity.class,
                DashboardActivity.class,
                StartActivity.class})
public class TessModule {
    @Provides
    @Singleton
    RestClient provideRestClient() {
        return new RestClient();
    }
}
