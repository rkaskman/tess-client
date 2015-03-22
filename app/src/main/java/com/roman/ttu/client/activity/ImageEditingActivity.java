package com.roman.ttu.client.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.TessClient.R;
import com.roman.ttu.client.SharedPreferencesConfig;

import java.io.File;
import java.io.FileOutputStream;

import static android.graphics.Bitmap.Config;
import static android.view.View.OnTouchListener;
import static android.view.View.VISIBLE;

public class ImageEditingActivity extends Activity {
    public static final String APP_IMAGE_DIRECTORY = "TessApp";
    public static final String IMAGE_FILE = "imageFile";
    ImageView resultImageView;
    ImageView drawingImageView;

    Canvas parentCanvas;
    Canvas drawingCanvas;

    Bitmap parentBitmap;
    Bitmap drawingBitmap;

    Point startPoint;
    Point endPoint;

    Paint paint;

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_edit);

        button = (Button) findViewById(R.id.save_picture_button);
        button.setVisibility(View.INVISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setVisibility(View.GONE);
                Point rectangleStartPoint = getStartForCroppedBitmap();
                Bitmap cropped = Bitmap.createBitmap(parentBitmap, rectangleStartPoint.x, rectangleStartPoint.y,
                        Math.abs(startPoint.x - endPoint.x), Math.abs(startPoint.y - endPoint.y));

                createImageFileFromCroppedBitmap(cropped);
            }
        });

        resultImageView = (ImageView) findViewById(R.id.result_image);
        drawingImageView = (ImageView) findViewById(R.id.drawing_pane);

        Intent sourceIntent = getIntent();
        Uri fileUri = sourceIntent.getParcelableExtra("imageFileUri");


        File imageFile = new File(fileUri.getPath());
        if (imageFile.exists()) {
            initializeDrawingComponents(imageFile);
        }

        String toastMessage = sourceIntent.getStringExtra("toastMessage");
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
    }

    private void createImageFileFromCroppedBitmap(Bitmap cropped) {
        File imagesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                APP_IMAGE_DIRECTORY);

        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesConfig.PREFERENCE_KEY, 0);

        String filename = sharedPreferences.getString(SharedPreferencesConfig.GOOGLE_USER_EMAIL, null)
                + "_" + System.currentTimeMillis() + ".jpg";
        File destinationFile = new File(imagesDir, filename);

        try {
            FileOutputStream out = new FileOutputStream(destinationFile);
            cropped.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            saveImageToMediaStore(destinationFile);
            button.setOnClickListener(null);

            Intent returnIntent = new Intent();
            returnIntent.putExtra(IMAGE_FILE, destinationFile);
            setResult(RESULT_OK, returnIntent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to store images", Toast.LENGTH_LONG).show();
        }
    }

    private void saveImageToMediaStore(File destinationFile) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, destinationFile.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, "desc");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, destinationFile.getAbsolutePath());

        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private Point getStartForCroppedBitmap() {
        if (startPoint.x < endPoint.x && startPoint.y < endPoint.y) {
            return startPoint;
        } else if (startPoint.x < endPoint.x && startPoint.y > endPoint.y) {
            return new Point(startPoint.x, endPoint.y);
        } else if (startPoint.x > endPoint.x && startPoint.y < endPoint.y) {
            return new Point(endPoint.x, startPoint.y);
        } else if (startPoint.x > endPoint.x && startPoint.y > endPoint.y) {
            return endPoint;
        }

        return null;
    }

    private void initializeDrawingComponents(File imageFile) {
        Bitmap sourceBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        Config bitmapConfig = sourceBitmap.getConfig() != null ? sourceBitmap.getConfig() : Config.ARGB_8888;

        parentBitmap = Bitmap.createBitmap(
                sourceBitmap.getWidth(),
                sourceBitmap.getHeight(),
                bitmapConfig);
        parentCanvas = new Canvas(parentBitmap);
        parentCanvas.drawBitmap(sourceBitmap, 0, 0, null);
        resultImageView.setImageBitmap(parentBitmap);

        drawingBitmap = Bitmap.createBitmap(
                sourceBitmap.getWidth(),
                sourceBitmap.getHeight(),
                bitmapConfig);

        drawingCanvas = new Canvas(drawingBitmap);
        drawingImageView.setImageBitmap(drawingBitmap);
        initializePaint();
        resultImageView.setOnTouchListener(touchEventListener);
    }

    private void initializePaint() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(15);
        paint.setStyle(Paint.Style.STROKE);
    }

    private void drawOnCanvas(ImageView view, float x, float y) {
        if (touchOutSideView(view, x, y)) {
            return;
        } else {
            float scaledX = x * parentBitmap.getWidth() / view.getWidth();
            float scaledY = y * parentBitmap.getHeight() / view.getHeight();

            drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            drawingCanvas.drawRect(startPoint.x, startPoint.y, scaledX, scaledY, paint);
            drawingImageView.invalidate();
        }
    }

    private boolean touchOutSideView(ImageView view, float x, float y) {
        return x < 0 || y < 0 || x > view.getWidth() || y > view.getHeight();
    }

    private Point getScaledPoint(ImageView view, float x, float y) {
        if (touchOutSideView(view, x, y)) {
            return null;
        } else {
            int scaledX = (int) (x * parentBitmap.getWidth() / view.getWidth());
            int scaledY = (int) (y * parentBitmap.getHeight() / view.getHeight());
            return new Point(scaledX, scaledY);
        }
    }

    OnTouchListener touchEventListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (button.getVisibility() == VISIBLE) {
                        button.setVisibility(View.INVISIBLE);
                    }
                    startPoint = getScaledPoint(imageView, x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawOnCanvas(imageView, x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    drawOnCanvas(imageView, x, y);
                    endPoint = getScaledPoint(imageView, x, y);
                    button.setVisibility(VISIBLE);
                    break;
            }
            return true;
        }
    };
}