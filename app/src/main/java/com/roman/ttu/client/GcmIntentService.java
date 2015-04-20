package com.roman.ttu.client;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.roman.ttu.client.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.roman.ttu.client.activity.ResponseHandlingActivity;
import com.roman.ttu.client.GcmBroadcastReceiver;
import com.roman.ttu.client.model.RecognitionErrorResponse;
import com.roman.ttu.client.model.RecognizedReceiptData;

import java.math.BigDecimal;
import java.util.Date;

public class GcmIntentService extends IntentService {

    private NotificationManagerCompat  mNotificationManager;

    private static final  String TAG = "GcmIntentService";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {

            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
//
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if(hasNoError(extras)) {
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
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ResponseHandlingActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_error_white_48dp)
                        .setContentTitle("Recognition error")
                        .setGroup("group_key_tess")
                        .setContentText("Following error ocurred - "  +errorResponse.message+ "\n," +
                                "recognized company reg. number is - "+ errorResponse.recognizedRegNr +"\n,"+
                                "recognized total cost  is '" + errorResponse.recognizedTotalCost+"'");

        mBuilder.setContentIntent(contentIntent);
        int notificationId = getNotificationId();
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
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ResponseHandlingActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_done_white_48dp)
                        .setContentTitle("Succesful recognition")
                        .setGroup("group_key_tess_success")
                        .setContentText("Company - "  + recognizedReceiptData.companyName+ "\n," +
                                "total cost - "+ recognizedReceiptData.totalCost+" "+recognizedReceiptData.currency);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(getNotificationId(), mBuilder.build());
    }

    private void initNotificationManager() {
        if(mNotificationManager == null) {
            mNotificationManager =
                    NotificationManagerCompat.from(this);
        }
    }
}