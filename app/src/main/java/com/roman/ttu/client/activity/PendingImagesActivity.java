package com.roman.ttu.client.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.roman.ttu.client.R;
import com.roman.ttu.client.SharedPreferenceManager;
import com.roman.ttu.client.adapter.PendingImagesAdapter;
import com.roman.ttu.client.db.PendingImagesDAO;
import com.roman.ttu.client.model.ImageStoredInDatabase;
import com.roman.ttu.client.model.ImagesWrapper;
import com.roman.ttu.client.model.UserImagesWrapper;
import com.roman.ttu.client.rest.ImagePostingService;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;
import com.roman.ttu.client.util.IOUtil;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.view.View.OnClickListener;
import static com.roman.ttu.client.SharedPreferenceManager.USER_ID;
import static com.roman.ttu.client.adapter.PendingImagesAdapter.IMAGES_KEY;
import static com.roman.ttu.client.util.IOUtil.getFileExtension;

public class PendingImagesActivity extends AuthenticationAwareActivity {

    @Inject
    PendingImagesDAO pendingImagesDAO;
    @Inject
    ImagePostingService imagePostingService;

    private ViewPager viewPager;
    private PendingImagesAdapter pendingImagesAdapter;

    private ImagesPostingCallBack imagesPostingCallBack = new ImagesPostingCallBack();
    private Integer currentItem = null;
    private Button send;
    private Button delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_images);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        initializeContent(findUserImages());
    }

    private void initializeContent(Collection<ImageStoredInDatabase> userImagesWrappers) {
        if (!userImagesWrappers.isEmpty()) {
            initializeAdapter(userImagesWrappers);
            initializeViewPagerContent();
            initializeButtons();

        } else {
            hideViewPager();
            showNoImagesToSend();
        }
    }

    private void initializeButtons() {
        send = (Button) findViewById(R.id.button_send);
        send.setOnClickListener(onClickSend);

        delete = (Button) findViewById(R.id.button_delete);
        delete.setOnClickListener(onClickDelete);
    }

    private void showNoImagesToSend() {
        View noImagesToSend = findViewById(R.id.no_images_to_send);
        noImagesToSend.setVisibility(View.VISIBLE);
    }

    private void initializeViewPagerContent() {
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(pendingImagesAdapter);
        UnderlinePageIndicator pageIndicator = (UnderlinePageIndicator) findViewById(R.id.images_page_indicator);
        pageIndicator.setViewPager(viewPager);
    }

    private void setButtonsEnabled(boolean enabled) {
        send.setEnabled(enabled);
        delete.setEnabled(enabled);
    }

    private void hideViewPager() {
        viewPager.setVisibility(View.GONE);
        findViewById(R.id.images_page_indicator).setVisibility(View.GONE);
        findViewById(R.id.buttons_bottom).setVisibility(View.GONE);
    }

    private Collection<ImageStoredInDatabase> findUserImages() {
        return pendingImagesDAO.find(preferenceManager.getString(USER_ID));
    }

    private void initializeAdapter(Collection<ImageStoredInDatabase> userImagesWrappers) {
        pendingImagesAdapter = new PendingImagesAdapter(getSupportFragmentManager(), userImagesWrappers);
    }

    public static class ImagesFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(
                    R.layout.view_pending_images, container, false);

            Bundle args = getArguments();
            ImageStoredInDatabase userImagesWrapper = (ImageStoredInDatabase) args.get(IMAGES_KEY);


            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap receiptImageBitmap = BitmapFactory.decodeFile(userImagesWrapper.imageFile.getPath(), options);

            ImageView receiptImageView = (ImageView) rootView.findViewById(R.id.receipt_image);

            receiptImageView.setImageBitmap(receiptImageBitmap);

            return rootView;
        }
    }

    private void postImages() {
        currentItem = viewPager.getCurrentItem();
        ImageStoredInDatabase imageStoredInDatabase = pendingImagesAdapter.getUserImagesWrapperBy(currentItem);
        UserImagesWrapper userImagesWrapper = null;
        try {
            userImagesWrapper = convert(imageStoredInDatabase);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to read image files", Toast.LENGTH_LONG).show();
            return;
        }

        userImagesWrapper.registrationId = preferenceManager.getString(SharedPreferenceManager.GCM_REGISTRATION_ID);
        imagePostingService.postImages(userImagesWrapper, imagesPostingCallBack);
    }

    private UserImagesWrapper convert(ImageStoredInDatabase imageStoredInDatabase) throws IOException {
        File imageFile = imageStoredInDatabase.imageFile;

        String encodedImage = Base64.encodeToString(IOUtil.readFile(imageFile), Base64.DEFAULT);
        ImagesWrapper.ImageWrapper imageWrapper = new ImagesWrapper.ImageWrapper(encodedImage, getFileExtension(imageFile.getName()));

        UserImagesWrapper userImagesWrapper = new UserImagesWrapper(imageStoredInDatabase.id,
                imageWrapper,
                null,
                imageStoredInDatabase.creationTime);


        return userImagesWrapper;
    }

    private OnClickListener onClickSend = new OnClickListener() {
        @Override
        public void onClick(View v) {
            progressDialog.show();
            setButtonsEnabled(false);
            postImages();
        }
    };

    private OnClickListener onClickDelete = new OnClickListener() {
        @Override
        public void onClick(View v) {
            progressDialog.show();
            setButtonsEnabled(false);
            currentItem = viewPager.getCurrentItem();
            handleImagesDeletion();
        }
    };

    public class ImagesPostingCallBack extends AuthenticationAwareActivityCallback {
        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
            handleImagesDeletion();
            Toast.makeText(PendingImagesActivity.this,
                    getString(R.string.images_posting_succeeded), Toast.LENGTH_LONG).show();
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.dismiss();
            Toast.makeText(PendingImagesActivity.this,
                    error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleImagesDeletion() {
        ImageStoredInDatabase imageStoredInDatabase = pendingImagesAdapter.getUserImagesWrapperBy(currentItem);
        pendingImagesDAO.delete(imageStoredInDatabase.id);
        pendingImagesAdapter.remove(currentItem);
        progressDialog.dismiss();

        if (pendingImagesAdapter.hasItems()) {
            setButtonsEnabled(true);
        } else {
            hideViewPager();
            showNoImagesToSend();
        }

    }
}