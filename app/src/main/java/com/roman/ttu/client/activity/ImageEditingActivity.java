package com.roman.ttu.client.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.TessClient.R;

import java.io.File;

import static android.graphics.Bitmap.Config;
import static android.view.View.OnTouchListener;
import static android.view.View.VISIBLE;

public class ImageEditingActivity extends Activity {
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

        resultImageView = (ImageView) findViewById(R.id.result_image);
        drawingImageView = (ImageView) findViewById(R.id.drawing_pane);

        Uri fileUri = getIntent().getParcelableExtra("imageFileUri");

        File imageFile = new File(fileUri.getPath());
        if (imageFile.exists()) {
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
    }

    private void initializePaint() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5 * 3);
        paint.setStyle(Paint.Style.STROKE);
    }

    private void drawOnDrawingCanvas(ImageView view, float x, float y) {
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

    private Point getStartPoint(ImageView view, float x, float y) {
        if (touchOutSideView(view, x, y)) {
            return null;
        } else {
            int scaledX = (int) (x * parentBitmap.getWidth() / view.getWidth());
            int scaledY = (int) (y * parentBitmap.getHeight() / view.getHeight());
            return new Point(scaledX, scaledY);
        }
    }

//    private void drawFinal() {
//        parentCanvas.drawBitmap(drawingBitmap, 0, 0, null);
//    }

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
                    startPoint = getStartPoint(imageView, x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawOnDrawingCanvas(imageView, x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    drawOnDrawingCanvas(imageView, x, y);
                    endPoint = new Point((int) x, (int) y);
                    button.setVisibility(VISIBLE);
                    break;
            }
            return true;
        }
    };
}