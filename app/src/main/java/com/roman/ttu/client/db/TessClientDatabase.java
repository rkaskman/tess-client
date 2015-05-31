package com.roman.ttu.client.db;

import android.provider.BaseColumns;

public final class TessClientDatabase {

    private TessClientDatabase() {
    }

    public static abstract class UserPendingImages implements BaseColumns {
        public static final String TABLE_NAME = "user_pending_images";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_RECEIPT_IMAGE = "receipt_image";
        public static final String COLUMN_NAME_INSERTED_AT = "inserted_at";
    }
}