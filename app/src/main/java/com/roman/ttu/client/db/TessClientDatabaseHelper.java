package com.roman.ttu.client.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.roman.ttu.client.db.TessClientDatabase.*;

public class TessClientDatabaseHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String TIMESTAMP_TYPE = " TIMESTAMP";
    private static final String NOT_NULL = " NOT NULL";

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TessClient.db";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_USER_PENDING_IMAGES =
            "CREATE TABLE " + UserPendingImages.TABLE_NAME + " (" +
                    UserPendingImages._ID + " INTEGER PRIMARY KEY," +
                    UserPendingImages.COLUMN_NAME_USER_ID + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    UserPendingImages.COLUMN_NAME_ENTERPRISE_ID_IMAGE + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    UserPendingImages.COLUMN_NAME_ENTERPRISE_ID_FILE_EXTENSION + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    UserPendingImages.COLUMN_NAME_TOTAL_COST_IMAGE + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    UserPendingImages.COLUMN_NAME_TOTAL_COST_FILE_EXTENSION + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    UserPendingImages.COLUMN_NAME_INSERTED_AT + TIMESTAMP_TYPE +
                    " DEFAULT CURRENT_TIMESTAMP" + NOT_NULL + ");";


    private static final String SQL_DELETE_PENDING_IMAGES =
            "DROP TABLE IF EXISTS " + UserPendingImages.TABLE_NAME;

    public TessClientDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER_PENDING_IMAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}