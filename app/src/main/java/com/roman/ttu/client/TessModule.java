package com.roman.ttu.client;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true,
        injects = SignInActivity.class)
public class TessModule {
    @Provides
    @Singleton
    RestClient provideRestClient() {
        return new RestClient();
    }
}
