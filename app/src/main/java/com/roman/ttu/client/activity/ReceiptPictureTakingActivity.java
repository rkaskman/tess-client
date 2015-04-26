package com.roman.ttu.client.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.roman.ttu.client.R;
import com.roman.ttu.client.Application;
import com.roman.ttu.client.db.PendingImagesDAO;
import com.roman.ttu.client.rest.ImagePostingService;
import com.roman.ttu.client.util.IOUtil;
import com.roman.ttu.client.model.ImagesWrapper;
import com.roman.ttu.client.service.AuthenticationAwareActivityCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.roman.ttu.client.SharedPreferenceManager.GCM_REGISTRATION_ID;
import static com.roman.ttu.client.SharedPreferenceManager.USER_ID;
import static com.roman.ttu.client.activity.ImageEditingActivity.IMAGE_FILE;
import static com.roman.ttu.client.model.ImagesWrapper.ImageWrapper;
import static com.roman.ttu.client.util.IOUtil.getFileExtension;

public class ReceiptPictureTakingActivity extends AuthenticationAwareActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int EDIT_IMAGE = 7001;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int REQUEST_CODE_POST_IMAGES_LOGIN = 12345;
    public static final String IMAGE_PREFIX = "IMG_";
    public static final String IMAGE_EXTENSION = ".jpg";

    private Button goButton;
    private Button takeImagesAgainButton;
    private Button saveButton;
    private Button retrySendButton;

    private File imageWithRegNumber;
    private File imageWithTotalCost;

    private File firstImage;
    private File secondImage;

    private Uri firstImageUri;
    private Uri secondImageUri;

    private ImagesWrapper imagesWrapper;

    private View startView;
    private View sendingView;
    private View successView;
    private View errorView;

    private enum Phase {
        START, SENDING, SUCCESS, ERROR
    }

    @Inject
    PendingImagesDAO pendingImagesDAO;

    private ImagePostingCallback imagePostingCallback = new ImagePostingCallback();

    @Inject
    ImagePostingService imagePostingService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getApplication()).getObjectGraph().inject(this);

        setContentView(R.layout.activity_receipt_pic_taking);
        initLayoutElements();
        initButtons();
        setPhase(Phase.START);
    }

    private void initButtons() {
        takeImagesAgainButton = (Button) findViewById(R.id.take_images_again_button);
        takeImagesAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPhase(Phase.START);
            }
        });

        goButton = (Button) findViewById(R.id.proceed_to_images_taking_button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageWithRegNumber = null;
                imageWithTotalCost = null;
                startCamera();
            }
        });

        saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImages();
            }
        });

        retrySendButton = (Button) findViewById(R.id.button_retry_send);
        retrySendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImages();
            }
        });
    }

    private void saveImages() {
        pendingImagesDAO.save(imagesWrapper, preferenceManager.getString(USER_ID));
        proceedToSuccessStage(true);
    }

    private void initLayoutElements() {
        startView = findViewById(R.id.receipt_pic_taking_start_stage);
        sendingView = findViewById(R.id.receipt_pic_taking_sending_stage);
        successView = findViewById(R.id.receipt_pic_taking_success_stage);
        errorView = findViewById(R.id.receipt_pic_taking_tech_error);
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imageWithRegNumber == null) {
            firstImage = getOutputMediaFile();
            firstImageUri = getOutputMediaFileUri(firstImage);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, firstImageUri);

        } else {
            secondImage = getOutputMediaFile();
            secondImageUri = getOutputMediaFileUri(secondImage);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, secondImageUri);
        }

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                editImage();
            } else {
                clearFiles();
            }
        }

        if (requestCode == EDIT_IMAGE && resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK) {
                File editedImageFile = (File) intent.getSerializableExtra(IMAGE_FILE);
                try {
                    resolveEditedImage(editedImageFile);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to read image files", Toast.LENGTH_LONG).show();
                }
            } else {
                clearFiles();
            }
        }

        if (requestCode == REQUEST_CODE_POST_IMAGES_LOGIN) {
            if (resultCode == RESULT_OK) {
                setPhase(Phase.SENDING);
                imagePostingService.postImages(imagesWrapper, imagePostingCallback);
            } else if (resultCode == RESULT_CANCELED) {
                finishActivityAndShowAuthError();
            }
        }
    }

    private void editImage() {
        Intent imageEditingIntent = new Intent(this, ImageEditingActivity.class);
        imageEditingIntent.putExtra("imageFileUri", imageWithRegNumber == null ? firstImageUri : secondImageUri);
        imageEditingIntent.putExtra("toastMessage",
                imageWithRegNumber == null ? "Mark registration number" : "Mark total cost");
        startActivityForResult(imageEditingIntent, EDIT_IMAGE);
    }

    private void resolveEditedImage(File editedImageFile) throws IOException {
        if (imageWithRegNumber == null) {
            imageWithRegNumber = editedImageFile;
            startCamera();

        } else if (imageWithTotalCost == null) {
            imageWithTotalCost = editedImageFile;
            processImages();
        }
    }

    private void processImages() throws IOException {
        imagesWrapper = new ImagesWrapper(getImageWrapperFor(imageWithRegNumber),
                getImageWrapperFor(imageWithTotalCost));
        imagesWrapper.registrationId = preferenceManager.getString(GCM_REGISTRATION_ID);

        if (isDeviceOnline()) {
            sendImages();
        } else {
            proposeToSaveImages();
        }
    }

    @Override
    protected void showNoConnectionAvailableError() {
        //allow to work offline
    }

    private void sendImages() {
        if (!sessionExpired()) {
            setPhase(Phase.SENDING);
            imagePostingService.postImages(imagesWrapper, imagePostingCallback);
        } else {
            Intent loginIntent = new Intent(ReceiptPictureTakingActivity.this, StartActivity.class);
            startActivityForResult(loginIntent, REQUEST_CODE_POST_IMAGES_LOGIN);
        }
    }

    private void setPhase(Phase phase) {
        startView.setVisibility(phase == Phase.START ? View.VISIBLE : View.GONE);
        sendingView.setVisibility(phase == Phase.SENDING ? View.VISIBLE : View.GONE);
        successView.setVisibility(phase == Phase.SUCCESS ? View.VISIBLE : View.GONE);
        errorView.setVisibility(phase == Phase.ERROR ? View.VISIBLE : View.GONE);
    }

    private void proposeToSaveImages() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.no_connection_detected))
                .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       saveImages();
                    }
                })
                .setNegativeButton(getString(R.string.no_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearFiles();
                    }
                })
                .show();
    }

    private void proceedToSuccessStage(boolean imagesSaved) {
        setPhase(Phase.SUCCESS);
        TextView imagesProceededView = (TextView) successView.findViewById(R.id.images_successfully_proceeded_info);
        imagesProceededView.setText(getString(imagesSaved ? R.string.images_saved_successfully : R.string.images_sent_successfully));
        clearFiles();
    }

    private ImageWrapper getImageWrapperFor(File imageFile) throws IOException {
        String encodedImage = Base64.encodeToString(IOUtil.readFile(imageFile), Base64.DEFAULT);
        return new ImageWrapper(encodedImage, getFileExtension(imageFile.getName()));
    }

    private Uri getOutputMediaFileUri(File f) {
        return Uri.fromFile(f);
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TessClient");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("TessClient", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                IMAGE_PREFIX + timeStamp + IMAGE_EXTENSION);
    }

    private void clearFiles() {
        IOUtil.deleteFile(firstImage);
        IOUtil.deleteFile(secondImage);
        IOUtil.deleteFile(imageWithRegNumber);
        IOUtil.deleteFile(imageWithTotalCost);

        firstImage = null;
        secondImage = null;
        imageWithRegNumber = null;
        imageWithTotalCost = null;

        firstImageUri = null;
        secondImageUri = null;
    }

    public class ImagePostingCallback extends AuthenticationAwareActivityCallback {
        @Override
        public void success(Object o, Response response) {
            super.success(o, response);
            proceedToSuccessStage(false);
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            setPhase(Phase.ERROR);
        }
    }
}