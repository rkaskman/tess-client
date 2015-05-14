package com.roman.ttu.client;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private Properties properties;

    public Configuration(Context context) {
        properties = new Properties();
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open("configuration.properties");
            properties.load(is);
            System.out.println();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBaseUrl() {
        return properties.getProperty("baseUrl");
    }

    public String getPort() {
        return properties.getProperty("port");
    }

    public String getTrustStorePassword() {
        return properties.getProperty("trustStorePassword");
    }
}