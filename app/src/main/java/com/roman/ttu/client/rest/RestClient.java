package com.roman.ttu.client.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.roman.ttu.client.Configuration;
import com.roman.ttu.client.rest.security.CertificateTrustManager;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class RestClient {
    private RestAdapter restAdapter;
    private Configuration configuration;

    public RestClient(Configuration configuration, CertificateTrustManager certificateTrustManager) {
        this.configuration = configuration;
        createRestAdapter(certificateTrustManager);
        initCookieManager();
    }

    private void initCookieManager() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    private void createRestAdapter(CertificateTrustManager certificateTrustManager) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();

        restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(composeUrl())
                .setConverter(new GsonConverter(gson))
                .setClient(getHttpClientForTestingPurposes(certificateTrustManager))
                .build();
    }

    private String composeUrl() {
        return configuration.getBaseUrl().concat(":").concat(configuration.getPort());
    }

    public <T> T create(Class<T> serviceClass) {
        return restAdapter.create(serviceClass);
    }

    private OkClient getHttpClientForTestingPurposes(CertificateTrustManager certificateTrustManager) {
        try {

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] {certificateTrustManager}, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return new OkClient(okHttpClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
