package com.roman.ttu.client.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.roman.ttu.client.model.ImageStoredInDatabase;
import com.roman.ttu.client.model.ImagesWrapper;
import com.roman.ttu.client.model.UserImagesWrapper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.roman.ttu.client.db.TessClientDatabase.*;
import static com.roman.ttu.client.model.ImagesWrapper.*;

public class PendingImagesDAO {

    TessClientDatabaseHelper dbHelper;

    public PendingImagesDAO(Context context) {
        dbHelper = new TessClientDatabaseHelper(context);
    }

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void save(File receiptImage, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE, receiptImage.getAbsolutePath());
        contentValues.put(UserPendingImages.COLUMN_NAME_USER_ID, userId);
        db.insert(UserPendingImages.TABLE_NAME, null, contentValues);
    }

    public Collection<ImageStoredInDatabase> find(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                UserPendingImages._ID,
                UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE,
                UserPendingImages.COLUMN_NAME_USER_ID,
                UserPendingImages.COLUMN_NAME_INSERTED_AT
        };

        String selection = UserPendingImages.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {userId};
        String sortOrder = UserPendingImages.COLUMN_NAME_INSERTED_AT + " DESC";

        Cursor c = db.query(UserPendingImages.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        return mapUserPendingImages(c);
    }

    public void delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(UserPendingImages.TABLE_NAME, UserPendingImages._ID + "=?", new String[]{String.valueOf(id)});
    }

    private Collection<ImageStoredInDatabase> mapUserPendingImages(Cursor c) {
        Set<ImageStoredInDatabase> userImages = new HashSet<>();
        if (c != null && c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex(UserPendingImages._ID));
                String receiptImagePath = c.getString(c.getColumnIndex(UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE));
                String insertedAtString = c.getString(c.getColumnIndex(UserPendingImages.COLUMN_NAME_INSERTED_AT));

                Date insertedAt = null;
                try {
                    insertedAt = parseDate(insertedAtString);
                } catch (ParseException ignored) {}


                ImageStoredInDatabase image = new ImageStoredInDatabase();

                image.id = id;
                image.creationTime = insertedAt;
                image.imageFile = new File(receiptImagePath);
                userImages.add(image);

            } while (c.moveToNext());
        }

        return userImages;
    }

    private Date parseDate(String insertedAtString) throws ParseException {
        return SDF.parse(insertedAtString);
    }
}