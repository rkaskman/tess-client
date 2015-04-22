package com.roman.ttu.client;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.roman.ttu.client.activity.ResponseHandlingActivity;
import com.roman.ttu.client.model.RecognitionErrorResponse;
import com.roman.ttu.client.model.RecognizedReceiptData;

import java.math.BigDecimal;
import java.util.Date;

public class GcmIntentService extends IntentService {

    public static final String EXTRA_RECOGNITION_ERROR_RESPONSE = "errorResponse";
    public static final String EXTRA_NOTIFICATION_ID = "notificationId";
    public static final String EXTRA_RECOGNIZED_RECEIPT_DATA = "recognizedReceiptData";
    private static final String NOTIFICATION_ACTION = "notificationAction";
    private NotificationManagerCompat mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (hasNoError(extras)) {
                    RecognizedReceiptData recognizedReceiptData = getRecognizedReceiptData(extras);
                    sendNotification(recognizedReceiptData);

                } else {
                    RecognitionErrorResponse recognitionErrorResponse = getRecognitionErrorResponse(extras);
                    sendNotification(recognitionErrorResponse);
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private RecognitionErrorResponse getRecognitionErrorResponse(Bundle extras) {
        RecognitionErrorResponse recognitionErrorResponse = new RecognitionErrorResponse();
        recognitionErrorResponse.message = extras.getString("message");
        recognitionErrorResponse.recognizedRegNr = extras.getString("recognizedRegNr");
        recognitionErrorResponse.recognizedTotalCost = extras.getString("recognizedTotalCost");
        return recognitionErrorResponse;
    }

    private RecognizedReceiptData getRecognizedReceiptData(Bundle extras) {
        RecognizedReceiptData recognizedReceiptData = new RecognizedReceiptData();
        recognizedReceiptData.totalCost = new BigDecimal(extras.getString("totalCost"));
        recognizedReceiptData.companyId = extras.getString("companyRegNumber");
        recognizedReceiptData.companyName = extras.getString("companyName");
        recognizedReceiptData.currency = extras.getString("currency");
        recognizedReceiptData.recognitionId = extras.getString("id");
        return recognizedReceiptData;
    }

    private boolean hasNoError(Bundle extras) {
        return extras.getString("message") == null;
    }

    private void sendNotification(RecognitionErrorResponse errorResponse) {
        initNotificationManager();
        Intent intent = createHandlingActivityIntent();
        int notificationId = getNotificationId();
        intent.putExtra(EXTRA_RECOGNITION_ERROR_RESPONSE, errorResponse);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_error_white_48dp)
                        .setContentTitle(getString(R.string.notification_title_recognition_error))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setGroup("group_key_tess")
                        .setAutoCancel(true)
                        .setContentText(getString(R.string.notification_recognition_error));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    private int getNotificationId() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        return Integer.valueOf(last4Str);
    }

    private void sendNotification(RecognizedReceiptData recognizedReceiptData) {
        initNotificationManager();
        Intent intent = createHandlingActivityIntent();
        int notificationId = getNotificationId();
        intent.putExtra(EXTRA_RECOGNIZED_RECEIPT_DATA, recognizedReceiptData);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_done_white_48dp)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle(getString(R.string.notification_title_recognition_ok))
                        .setGroup("group_key_tess_success")
                        .setAutoCancel(true)
                        .setContentText(getString(R.string.notification_recognition_ok));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(getNotificationId(), mBuilder.build());
    }

    private Intent createHandlingActivityIntent() {
        Intent intent = new Intent(this, ResponseHandlingActivity.class);
        intent.setAction(NOTIFICATION_ACTION + System.currentTimeMillis());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private void initNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager =
                    NotificationManagerCompat.from(this);
        }
    }
}