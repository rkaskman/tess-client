package com.roman.ttu.client;


        import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
        import com.example.TessClient.R;
        import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DroidSimple extends Activity {

    private static final String SERVICE_URL = "http://192.168.1.9:8080/RestWebServiceDemo/rest/person";

    private static final String TAG = "AndroidRESTClientActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void retrieveSampleData(View vw) {
        String sampleURL = SERVICE_URL + "/sample";

        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...");
        wst.execute(new String[] { sampleURL });

    }
//
//    public void clearControls(View vw) {
//
//        EditText edFirstName = (EditText) findViewById(R.id.first_name);
//        EditText edLastName = (EditText) findViewById(R.id.last_name);
//        EditText edEmail = (EditText) findViewById(R.id.email);
//
//        edFirstName.setText("");
//        edLastName.setText("");
//        edEmail.setText("");
//
//    }

//    public void postData(View vw) {
//
//        EditText edFirstName = (EditText) findViewById(R.id.first_name);
//        EditText edLastName = (EditText) findViewById(R.id.last_name);
//        EditText edEmail = (EditText) findViewById(R.id.email);
//
//        String firstName = edFirstName.getText().toString();
//        String lastName = edLastName.getText().toString();
//        String email = edEmail.getText().toString();
//
//        if (firstName.equals("") || lastName.equals("") || email.equals("")) {
//            Toast.makeText(this, "Please enter in all required fields.",
//                    Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...");
//
//        wst.addNameValuePair("firstName", firstName);
//        wst.addNameValuePair("lastName", lastName);
//        wst.addNameValuePair("email", email);
//
//        // the passed String is the URL we will POST to
//        wst.execute(new String[] { SERVICE_URL });
//
//    }

    public void handleResponse(String response) {

//        EditText edFirstName = (EditText) findViewById(R.id.first_name);
//        EditText edLastName = (EditText) findViewById(R.id.last_name);
//        EditText edEmail = (EditText) findViewById(R.id.email);
//
//        edFirstName.setText("");
//        edLastName.setText("");
//        edEmail.setText("");
//
//        try {
//
//            JSONObject jso = new JSONObject(response);
//
//            String firstName = jso.getString("firstName");
//            String lastName = jso.getString("lastName");
//            String email = jso.getString("email");
//
//            edFirstName.setText(firstName);
//            edLastName.setText(lastName);
//            edEmail.setText(email);
//
//        } catch (Exception e) {
//            Log.e(TAG, e.getLocalizedMessage(), e);
//        }

    }

    private void hideKeyboard() {

        InputMethodManager inputManager = (InputMethodManager) DroidSimple.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(
                DroidSimple.this.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private class WebServiceTask extends AsyncTask<String, Integer, String> {

        public static final int POST_TASK = 1;
        public static final int GET_TASK = 2;

        private static final String TAG = "WebServiceTask";

        // connection timeout, in milliseconds (waiting to connect)
        private static final int CONN_TIMEOUT = 3000;

        // socket timeout, in milliseconds (waiting for data)
        private static final int SOCKET_TIMEOUT = 5000;

        private int taskType = GET_TASK;
        private Context mContext = null;
        private String processMessage = "Processing...";

        private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

        private ProgressDialog pDlg = null;

        public WebServiceTask(int taskType, Context mContext, String processMessage) {

            this.taskType = taskType;
            this.mContext = mContext;
            this.processMessage = processMessage;
        }

        public void addNameValuePair(String name, String value) {

            params.add(new BasicNameValuePair(name, value));
        }

        private void showProgressDialog() {
            pDlg = new ProgressDialog(mContext);
            pDlg.setMessage(processMessage);
            pDlg.setProgressDrawable(mContext.getWallpaper());
            pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDlg.setCancelable(false);
            pDlg.show();
        }

        @Override
        protected void onPreExecute() {
            hideKeyboard();
            showProgressDialog();
        }

        protected String doInBackground(String... urls) {

            String url = urls[0];
            String result = "";

            HttpResponse response = doResponse(url);

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

        @Override
        protected void onPostExecute(String response) {
            handleResponse(response);
            pDlg.dismiss();
        }

        private HttpParams getHttpParams() {
            HttpParams htpp = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
            HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

            return htpp;
        }

        private HttpResponse doResponse(String url) {
            HttpClient httpclient = new DefaultHttpClient(getHttpParams());

            HttpResponse response = null;

            try {
                switch (taskType) {

                    case POST_TASK:
                        HttpPost httppost = new HttpPost(url);
                        // Add parameters
                        httppost.setEntity(new UrlEncodedFormEntity(params));

                        response = httpclient.execute(httppost);
                        break;
                    case GET_TASK:
                        HttpGet httpget = new HttpGet(url);
                        response = httpclient.execute(httpget);
                        break;
                }
            } catch (Exception e) {

                Log.e(TAG, e.getLocalizedMessage(), e);

            }

            return response;
        }

        private String inputStreamToString(InputStream is) {

            String line = "";
            StringBuilder total = new StringBuilder();

            // Wrap a BufferedReader around the InputStream
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            try {
                // Read response until the end
                while ((line = rd.readLine()) != null) {
                    total.append(line);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            // Return full string
            return total.toString();
        }
    }
}