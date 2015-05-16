package com.roman.ttu.client.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.roman.ttu.client.model.ImagesWrapper;
import com.roman.ttu.client.model.UserImagesWrapper;

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

    public void save(ImagesWrapper imagesWrapper, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ImageWrapper regNumberImageWrapper = imagesWrapper.receiptImage;

        ContentValues contentValues = new ContentValues();

        contentValues.put(UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE, regNumberImageWrapper.encodedImage);
        contentValues.put(UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE_EXTENSION, regNumberImageWrapper.fileExtension);
        contentValues.put(UserPendingImages.COLUMN_NAME_USER_ID, userId);
        db.insert(UserPendingImages.TABLE_NAME, null, contentValues);
    }

    public Collection<UserImagesWrapper> find(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                UserPendingImages._ID,
                UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE,
                UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE_EXTENSION,

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

    private Collection<UserImagesWrapper> mapUserPendingImages(Cursor c) {
        Set<UserImagesWrapper> userImages = new HashSet<>();
        if (c != null && c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex(UserPendingImages._ID));
                String enterpriseIdImage = c.getString(c.getColumnIndex(UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE));
                String enterpriseIdFileExtension = c.getString(c.getColumnIndex(UserPendingImages.COLUMN_NAME_RECEIPT_IMAGE_EXTENSION));
                String insertedAtString = c.getString(c.getColumnIndex(UserPendingImages.COLUMN_NAME_INSERTED_AT));


                Date insertedAt;
                try {
                    insertedAt = parseDate(insertedAtString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                UserImagesWrapper userImagesWrapper = new UserImagesWrapper(id, new ImageWrapper(enterpriseIdImage, enterpriseIdFileExtension),
                        new ImageWrapper(null, null),
                        insertedAt);

                userImages.add(userImagesWrapper);

            } while (c.moveToNext());
        }

        return userImages;
    }

    private Date parseDate(String insertedAtString) throws ParseException {
        return SDF.parse(insertedAtString);
    }
}