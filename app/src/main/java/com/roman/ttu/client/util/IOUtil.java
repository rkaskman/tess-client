package com.roman.ttu.client.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import java.io.*;

import static android.text.TextUtils.isEmpty;

public class IOUtil {

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static String getFileExtension(String fileName) {
        int extensionSeparatorIndex = fileName.lastIndexOf(".");
        return !isEmpty(fileName) && extensionSeparatorIndex > 0 ? fileName.substring(extensionSeparatorIndex + 1) : null;
    }

    public static void safeDeleteFile(File f) {
        if(f != null && f.exists()) {
            f.delete();
        }
    }

    public static Bitmap decodeBase64Image(String base64Image) {
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}