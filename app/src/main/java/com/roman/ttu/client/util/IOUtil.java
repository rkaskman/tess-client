package com.roman.ttu.client.util;

import android.text.TextUtils;

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

    public static void deleteFile(File f) {
        if(f != null && f.exists()) {
            f.delete();
        }
    }
}