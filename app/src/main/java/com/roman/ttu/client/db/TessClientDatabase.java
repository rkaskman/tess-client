package com.roman.ttu.client.db;

import android.provider.BaseColumns;

public final class TessClientDatabase {

    private TessClientDatabase() {
    }

    public static abstract class UserPendingImages implements BaseColumns {
        public static final String TABLE_NAME = "user_pending_images";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_USER_ID = "userId";
        public static final String COLUMN_NAME_ENTERPRISE_ID_IMAGE = "enterprise_id_image";
        public static final String COLUMN_NAME_ENTERPRISE_ID_FILE_NAME = "enterprise_id_file_name";
        public static final String COLUMN_NAME_TOTAL_COST_IMAGE = "total_cost_image";
        public static final String COLUMN_NAME_TOTAL_COST_FILE_NAME = "total_cost_image";
        public static final String COLUMN_NAME_INSERTED_AT = "inserted_at";
    }
}