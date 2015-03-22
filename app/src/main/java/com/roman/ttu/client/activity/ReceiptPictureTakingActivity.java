package com.roman.ttu.client.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.TessClient.R;
import com.roman.ttu.client.IOUtil;
import com.roman.ttu.client.rest.ImagesWrapper;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.roman.ttu.client.activity.ImageEditingActivity.IMAGE_FILE;
import static com.roman.ttu.client.rest.ImagesWrapper.ImageWrapper;

public class ReceiptPictureTakingActivity extends AuthenticationAwareActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int EDIT_IMAGE = 7001;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Button button;

    private File imageWithRegNumber;
    private File imageWithTotalCost;

    private Uri fileUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageWithRegNumber = null;
                imageWithTotalCost = null;
                startCamera();
            }
        });
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent imageEditingIntent = new Intent(this, ImageEditingActivity.class);
            imageEditingIntent.putExtra("imageFileUri", fileUri);
            imageEditingIntent.putExtra("toastMessage",
                    imageWithRegNumber == null ? "Mark registration number" : "Mark total cost");
            startActivityForResult(imageEditingIntent, EDIT_IMAGE);
        } else if (requestCode == EDIT_IMAGE && resultCode == RESULT_OK) {
            File editedImageFile = (File) intent.getSerializableExtra(IMAGE_FILE);
            try {
                resolveEditedImage(editedImageFile);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to read image files", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void resolveEditedImage(File editedImageFile) throws IOException {
        if (imageWithRegNumber == null) {
            imageWithRegNumber = editedImageFile;
            startCamera();

        } else if (imageWithTotalCost == null) {
            imageWithTotalCost = editedImageFile;
            postImagesForOcr();
        }
    }

    private void postImagesForOcr() throws IOException {
        ImagesWrapper imagesWrapper = new ImagesWrapper(getImageWrapperFor(imageWithRegNumber), getImageWrapperFor(imageWithTotalCost));
        restClient.getImagePostingService().postImages(imagesWrapper, imagesPostingCallBack);
    }

    private ImageWrapper getImageWrapperFor(File imageFile) throws IOException {
        String encodedImage = Base64.encodeToString(IOUtil.readFile(imageFile), Base64.DEFAULT);
        String imageFileExtension = getImageFileExtension(imageFile);
        return new ImageWrapper(encodedImage, imageFileExtension);
    }

    private String getImageFileExtension(File file) {
        String filenameArray[] = file.getName().split("\\.");
        return filenameArray[filenameArray.length - 1];
    }

    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TessClient");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("TessClient", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

    }

    AuthenticationAwareActivityCallback imagesPostingCallBack = new AuthenticationAwareActivityCallback(this) {
        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
        }
    };
}