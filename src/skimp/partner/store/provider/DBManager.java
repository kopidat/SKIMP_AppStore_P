package skimp.partner.store.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Arrays;

public class DBManager extends SQLiteOpenHelper {
    private static final String TAG = DBManager.class.getSimpleName();

    private static final String DB_AUTH = "skimp.partner.store.provider.auth.db";
    private static final int DB_VERSION = 1;

    /** 암/복호화 key */
    private static final byte[] SECURE_KEY = {'c', 'o', 'm', '.', 's', 'k', 'i', 'm', 'p', '.', 'c', 'o', 'm', 'm', 'o', 'n',
            'c', 'o', 'm', '.', 's', 'k', 'i', 'm', 'p', '.', 'c', 'o', 'm', 'm', 'o', 'n'};
    /** 앱 해시키 */
    private static final byte[] APP_HASH_KEY = {'1', 'R', 'b', '4', 'O', 'l', 'R', 'G', 'Q', 'C', '0', 'm', 'Y', '1', '8', 'e',
            'o', 'X', 'Y', 'R', 'E', '+', '/', 'D', 'C', 'N', 'g', '='};

    /**
     * 현재 스토어에 로그인한 사용자
     */
    public static final class TBL_LOGIN_USER implements BaseColumns {
        public static final String NAME = "TBL_LOGIN_USER";

        /**
         * 현재 스토어 로그인 ID
         */
        public static final String COL_userId = "userId";
        /**
         * 스토어 로그인 후 발급받은 토큰 정보
         */
        public static final String COL_token = "token";
        /**
         * 암호화된 패스워드
         */
        public static final String COL_encPwd = "encPwd";
    }

    /** 암/복호화 key */
    public static final class TBL_KEY implements BaseColumns {
        public static final String NAME = "TBL_KEY";

        /** 암/복호화 key */
        public static final String COL_key = "key";
        /** 앱 해시키 */
        public static final String COL_app_hash = "app_hash";
    }

    private static DBManager instance = null;

    private DBManager(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbName, factory, version);
        Log.e(TAG, "DBManager(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version)");
        Log.e(TAG, "dbName = " + dbName);
        Log.e(TAG, "version = " + version);

    }

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context, DB_AUTH, null, DB_VERSION);
        }
        return instance;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.e(TAG, "onOpen(SQLiteDatabase db)");
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "onCreate(SQLiteDatabase db)");

        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)");
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TBL_LOGIN_USER.NAME);
//                db.execSQL("DROP TABLE IF EXISTS " + TBL_KEY.NAME);
        }
        onCreate(db);
    }

    public String getKey() {
        Cursor keyInfoCursor = instance.getReadableDatabase().query(TBL_KEY.NAME,
                null, null, null, null, null, null);

        if (keyInfoCursor != null) {
            try {
                if (keyInfoCursor.moveToFirst()) {
                    try {
                        String key = keyInfoCursor.getString(keyInfoCursor.getColumnIndexOrThrow(TBL_KEY.COL_key));
                        return key;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                keyInfoCursor.close();
            }
        }

        return null;
    }

    public String getHashKey() {
        Cursor keyInfoCursor = instance.getReadableDatabase().query(TBL_KEY.NAME,
                null, null, null, null, null, null);

        if (keyInfoCursor != null) {
            try {
                if (keyInfoCursor.moveToFirst()) {
                    try {
                        String key = keyInfoCursor.getString(keyInfoCursor.getColumnIndexOrThrow(TBL_KEY.COL_app_hash));
                        return key;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                keyInfoCursor.close();
            }
        }

        return null;
    }

//        public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
//            Log.i(TAG, "query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)");
//            Log.d(TAG, "columns = " + columns);
//            Log.d(TAG, "selection = " + selection);
//            Log.d(TAG, "selectionArgs = " + selectionArgs);
//            Log.d(TAG, "groupBy = " + groupBy);
//            Log.d(TAG, "having = " + having);
//            Log.d(TAG, "orderBy = " + orderBy);
//            return getReadableDatabase().query(TBL_KEY.NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
//        }
//
//        /**
//         * 스토어 로그인 사용자
//         * @param addRowValue
//         * @return
//         */
//        public long insert(ContentValues addRowValue) {
//            Log.i(TAG, "insert(ContentValues addRowValue)");
//            Log.d(TAG, "addRowValue = " + addRowValue);
////            return getWritableDatabase().insert(TBL_KEY.NAME, null, addRowValue);
//            return replace(TBL_KEY.NAME, null, addRowValue);
//        }
//
//        public int delete(String whereClause, String[] whereArgs) {
//            Log.i(TAG, "delete(String whereClause, String[] whereArgs)");
//            Log.d(TAG, "whereClause = " + whereClause);
//            Log.d(TAG, "whereArgs = " + whereArgs);
//            return getWritableDatabase().delete(TBL_KEY.NAME, whereClause, whereArgs);
//        }
//
//        public int insertAll(ContentValues[] values) {
//            Log.i(TAG, "insertAll(ContentValues[] values)");
//            SQLiteDatabase db = getWritableDatabase();
//
//            db.beginTransaction();
//
//            for (ContentValues contentValues : values) {
//                db.insert(TBL_KEY.NAME, null, contentValues);
//            }
//
//            db.setTransactionSuccessful();
//            db.endTransaction();
//
//            return values.length;
//        }
//
//        public int update(ContentValues updateRowValue, String whereClause, String[] whereArgs) {
//            Log.i(TAG, "update(ContentValues updateRowValue, String whereClause, String[] whereArgs)");
//            return getWritableDatabase().update(TBL_KEY.NAME, updateRowValue, whereClause, whereArgs);
//        }

    private void createTable(SQLiteDatabase db) {
        createLoginUserTable(db);
        createKeyTable(db);
    }

    private void createLoginUserTable(SQLiteDatabase db) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS " + TBL_LOGIN_USER.NAME + " (");
        sqlBuilder.append(TBL_LOGIN_USER._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");
        sqlBuilder.append(TBL_LOGIN_USER.COL_userId + " TEXT,");
        sqlBuilder.append(TBL_LOGIN_USER.COL_encPwd + " TEXT,");
        sqlBuilder.append(TBL_LOGIN_USER.COL_token + " TEXT");
        sqlBuilder.append(");");
        db.execSQL(sqlBuilder.toString());
    }

    private void createKeyTable(SQLiteDatabase db) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS " + TBL_KEY.NAME + " (");
        sqlBuilder.append(TBL_KEY._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");
        sqlBuilder.append(TBL_KEY.COL_key + " TEXT,");
        sqlBuilder.append(TBL_KEY.COL_app_hash + " TEXT");
        sqlBuilder.append(");");
        db.execSQL(sqlBuilder.toString());

        // insert into user (_id, name, address) values (1, 'name', 'Seoul');
        db.execSQL(new StringBuilder().append("INSERT INTO ").append(DBManager.TBL_KEY.NAME)
                .append(" (")
                .append(DBManager.TBL_KEY.COL_key).append(",")
                .append(DBManager.TBL_KEY.COL_app_hash)
                .append(") VALUES( ")
                .append("'").append(new String(SECURE_KEY)).append("'").append(",")
                .append("'").append(new String(APP_HASH_KEY)).append("'")
                .append(" );")
                .toString());
        Arrays.fill(SECURE_KEY, (byte)0x20);
        Arrays.fill(APP_HASH_KEY, (byte)0x20);
    }

    public long replace(String table, String nullColumnHack, ContentValues initialValues) {
        Log.i(TAG, "replace(String table, String nullColumnHack, ContentValues initialValues)");
        Log.d(TAG, "table = " + table);
        Log.d(TAG, "nullColumnHack = " + nullColumnHack);
        Log.d(TAG, "initialValues = " + initialValues);
        return getWritableDatabase().insertWithOnConflict(table, nullColumnHack, initialValues, SQLiteDatabase.CONFLICT_REPLACE);
    }
}