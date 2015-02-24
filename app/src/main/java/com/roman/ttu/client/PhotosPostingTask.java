package com.roman.ttu.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.example.TessClient.R;
import com.roman.ttu.client.rest.CallbackAction;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.*;

import static android.content.Context.NOTIFICATION_SERVICE;

public class PhotosPostingTask extends AsyncTask<Void, Integer, String> {

    private File fileToPost;
    private CallbackAction callbackAction;

    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");

    public PhotosPostingTask(File fileToPost, Context mContext, CallbackAction callbackAction) {
        this.fileToPost = fileToPost;
        this.mContext = mContext;
        this.processMessage = "Posting image to ocr..";
        this.callbackAction = callbackAction;
    }

    private static final String TAG = "WebServiceTask";

    private static final int CONN_TIMEOUT = 3000;

    private static final int SOCKET_TIMEOUT = 10000;
    private Context mContext = null;
    private String processMessage = "Processing...";


    private ProgressDialog pDlg = null;

    private void showProgressDialog() {
        pDlg = new ProgressDialog(mContext);
        pDlg.setMessage(processMessage);
        pDlg.setProgressDrawable(android.app.WallpaperManager.getInstance(mContext).getDrawable());
        pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDlg.setCancelable(false);
        pDlg.show();
    }

    @Override
    protected void onPreExecute() {
        callbackAction.execute();
    }

    @Override
    protected void onPostExecute(String response) {
        System.out.println(response);
        createNotification(response, "", "");
//            handleResponse(response);
//        pDlg.dismiss();
    }

    private HttpParams getHttpParams() {
        HttpParams http = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(http,
                getResources().getInteger(R.integer.connectionTimeout));
        HttpConnectionParams.setSoTimeout(http, getResources().getInteger(R.integer.socketTimeout));
        return http;
    }

    private Resources getResources() {
        return mContext.getResources();
    }

    protected String doInBackground(Void... params) {
        String result = "";


        Request request = new Request.Builder()
                .url(getResources().getString(R.string.serviceUrl))
                .post(createRequestBody())
                .build();
        HttpResponse response = doResponse(getResources().getString(R.string.serviceUrl));
        if (response == null) {
            return result;
        } else {

            try {
                result = inputStreamToString(response.getEntity().getContent());
            } catch (IllegalStateException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);

            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    private RequestBody createRequestBody() {
        return  new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"regNrPart\""),
                        RequestBody.create(MEDIA_TYPE_JPEG, "Square Logo"))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"totalSumPart\""),
                        RequestBody.create(MEDIA_TYPE_JPEG, new File("website/static/logo-square.png")))
                .build();
    }

    private HttpResponse doResponse(String url) {
        OkHttpClient httpClient = new OkHttpClient();
        HttpClient httpclient = new DefaultHttpClient(getHttpParams());

        HttpResponse response = null;

        try {
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new InputStreamEntity(new FileInputStream(fileToPost), fileToPost.length()));
            response = httpclient.execute(httppost);

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return response;
    }

    private String inputStreamToString(InputStream is) {
        String line;
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        } finally {
            safeClose(rd);
        }

        return total.toString();
    }

    private void createNotification(String totalSum, String shopId, String shopName) {
        int notificationId = 001;
        Intent viewIntent = new Intent(mContext, SubmitOcrActivity.class);
        viewIntent.putExtra("TOTAL_SUM", totalSum);
        viewIntent.putExtra("SHOP_ID", shopId);
        viewIntent.putExtra("SHOP_NAME", shopName);

        PendingIntent viewPendingIntent = PendingIntent.getActivity(mContext, 0, viewIntent, 0);
        Notification.Builder notificationBuilder = new Notification.Builder(mContext).
                setSmallIcon(android.R.drawable.arrow_up_float).
                setContentTitle("Ocr complete").
                setContentText("Submit your expense").
                setContentIntent(viewPendingIntent);


        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        notificationManager.notify(notificationId, notification);
    }

    private void safeClose(Closeable c) {
        try {
            c.close();
        } catch (Exception ignored) {
        }
    }
}