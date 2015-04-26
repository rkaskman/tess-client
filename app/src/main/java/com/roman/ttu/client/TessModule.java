package com.roman.ttu.client;


import android.content.Context;

import com.roman.ttu.client.activity.DashboardActivity;
import com.roman.ttu.client.activity.ExpenseListActivity;
import com.roman.ttu.client.activity.PendingImagesActivity;
import com.roman.ttu.client.activity.ReceiptPictureTakingActivity;
import com.roman.ttu.client.activity.ResponseHandlingActivity;
import com.roman.ttu.client.activity.StartActivity;
import com.roman.ttu.client.db.PendingImagesDAO;
import com.roman.ttu.client.rest.ExpenseService;
import com.roman.ttu.client.rest.ImagePostingService;
import com.roman.ttu.client.rest.RestClient;
import com.roman.ttu.client.rest.SignInService;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true,
        injects = {DashboardActivity.class,
                StartActivity.class,
                ReceiptPictureTakingActivity.class,
                AuthenticationAwareActivityCallback.class,
                StartActivity.SignInCallBack.class,
                ReceiptPictureTakingActivity.ImagePostingCallback.class,
                ExpenseListActivity.class,
                ExpenseListActivity.ExpenseQueryCallback.class,
                ExpenseListActivity.ExpenseConfirmationCallback.class,
                ExpenseListActivity.ExpenseDeclineCallback.class,
                ResponseHandlingActivity.class,
                ResponseHandlingActivity.ResponseHandlingActivityCallback.class,
                PendingImagesActivity.class,
                PendingImagesActivity.ImagesPostingCallBack.class})
public class TessModule {

    public TessModule(Context context) {
        this.context = context;
    }

    private Context context;

    @Provides
    @Singleton
    RestClient provideRestClient() {
        return new RestClient();
    }

    @Provides
    @Singleton
    SignInService provideSignInService(RestClient restClient) {
        return restClient.create(SignInService.class);
    }

    @Provides
    @Singleton
    ImagePostingService provideImagePostingService(RestClient restClient) {
        return restClient.create(ImagePostingService.class);
    }

    @Provides
    @Singleton
    SharedPreferenceManager provideSharedPreferenceManager() {
        return new SharedPreferenceManager(context);
    }

    @Provides
    @Singleton
    PendingImagesDAO providePendingImagesDAO() {
        return new PendingImagesDAO(context);
    }

    @Provides
    @Singleton
    ExpenseService provideExpensesService(RestClient restClient) {
        return restClient.create(ExpenseService.class);
    }
}