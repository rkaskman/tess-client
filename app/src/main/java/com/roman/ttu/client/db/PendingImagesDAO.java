package com.roman.ttu.client.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.roman.ttu.client.rest.model.ImagesWrapper;

import java.util.Collection;

public class PendingImagesDAO {

    TessClientDatabaseHelper dbHelper;

    public PendingImagesDAO(Context context) {
        dbHelper = new TessClientDatabaseHelper(context);
    }

    public void save(ImagesWrapper imagesWrapper, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

    }


    public Collection<ImagesWrapper> find(String userId) {
        return null;
    }
}