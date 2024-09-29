package skimp.partner.store.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class AuthContentProvider extends ContentProvider {
    private static final String TAG = AuthContentProvider.class.getSimpleName();

    /** The authority of this content provider. */
    public static final String AUTHORITY = "skimp.partner.store.authprovider";

    /**
     * The URI for the TBL_LOGIN_USER table.
     * TBL_LOGIN_USER 테이블은 현재 스토어앱에 로그인한 단 한명의 사용자 정보만 저장됨.
     */
    public static final Uri URI_LOGIN_USER = Uri.parse("content://" + AUTHORITY + "/" + DBManager.TBL_LOGIN_USER.NAME);

    /** The URI for the TBL_KEY table. */
    public static final Uri URI_KEY = Uri.parse("content://" + AUTHORITY + "/" + DBManager.TBL_KEY.NAME);

    /** TBL_LOGIN_USER 테이블은 현재 스토어앱에 로그인한 단 한명의 사용자 정보만 저장됨. */
    private static final int LOGIN_USER_DIR = 0;

    private static final int KEY_DIR = 10;
//    private static final int KEY_ITEM = 11;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // URI 를 상수값으로 매치 시켜주는 메서드
    public static UriMatcher buildUriMatcher() {
        // UriMatcher.NO_MATCH 상수를 넘겨 빈 매처를 만든다.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        /*
         * addURI(String authority, String path, int code)
         * 첫 번째와 두 번째 인자는 합쳐져, 인식하고자 하는 URI 를 매처에게 알려준다.
         * 세번째 인자를 사용해 해당 URI 를 어느 상수에 매칭시킬지 설정한다.
         */
        // 디렉토리의 모든 행을 나타내는 URI - TBL_LOGIN_USER
        uriMatcher.addURI(AUTHORITY, DBManager.TBL_LOGIN_USER.NAME, LOGIN_USER_DIR);
//        // 하나의 행을 나타내는 URI - TBL_LOGIN_USER
//        uriMatcher.addURI(AUTHORITY, DBManager.TBL_LOGIN_USER.NAME + "/#", LOGIN_USER_ITEM);

        // 디렉토리의 모든 행을 나타내는 URI - TBL_KEY
        uriMatcher.addURI(AUTHORITY, DBManager.TBL_KEY.NAME, KEY_DIR);
//        // 하나의 행을 나타내는 URI - TBL_KEY
//        uriMatcher.addURI(AUTHORITY, DBManager.TBL_KEY.NAME + "/#", KEY_ITEM);

        return uriMatcher;
    }

    private static DBManager sDBManager;

    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
//        // TODO: Implement this to initialize your content provider on startup.
//        return false;
        sDBManager = DBManager.getInstance(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        Log.i(TAG, "getType(Uri uri) = " + uri);
        int match = sUriMatcher.match(uri);
        Log.d(TAG, "match = " + match);

        switch (match) {
            case LOGIN_USER_DIR:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + DBManager.TBL_LOGIN_USER.NAME;
            case KEY_DIR:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + DBManager.TBL_KEY.NAME;
//            case KEY_ITEM:
//                return "vnd.android.cursor.item/" + AUTHORITY + "." + DBManager.TBL_KEY.NAME;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)");
        Log.d(TAG, "uri = " + uri);
        Log.d(TAG, "projection = " + projection);
        Log.d(TAG, "selection = " + selection);
        Log.d(TAG, "selectionArgs = " + selectionArgs);
        Log.d(TAG, "sortOrder = " + sortOrder);

//        String table = DBManager.TBL_KEY.NAME;
        String table = DBManager.TBL_LOGIN_USER.NAME;
        switch(sUriMatcher.match(uri)){
            case LOGIN_USER_DIR:
                table = DBManager.TBL_LOGIN_USER.NAME;
                break;
//            case KEY_ITEM:
//                selection = DBManager.TBL_KEY._ID + "=" + ContentUris.parseId(uri);
//                selectionArgs = null;
//                break;
            case KEY_DIR:
                table = DBManager.TBL_KEY.NAME;
                break;
            case UriMatcher.NO_MATCH:
                return null;
        }
        // 새 데이터를 쓰기 위해 데이터베이스에 접근한다.
        final SQLiteDatabase db = sDBManager.getReadableDatabase();
        Cursor resultCursor = db.query(table, projection, selection, selectionArgs,null, null, sortOrder);
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return resultCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(TAG, "insert(Uri uri, ContentValues values) uri = " + uri);
        Log.d(TAG, "values = " + values);

        // 디렉토리에 매칭되는 URI 매칭 코드를 받는다.
        int match = sUriMatcher.match(uri);
        Log.d(TAG, "match = " + match);

        switch (match) {
            case LOGIN_USER_DIR:
            case KEY_DIR:
                return insertOrUpdate(match, uri, values);
            default:
                break;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(TAG, "delete(Uri uri, String selection, String[] selectionArgs)");
        Log.d(TAG, "selection = " + selection);
        Log.d(TAG, "selectionArgs = " + selectionArgs);

        int match = sUriMatcher.match(uri);
        Log.d(TAG, "match = " + match);
        int deletedRowCount = 0;

        switch (match){
            case LOGIN_USER_DIR: {
                final SQLiteDatabase db = sDBManager.getWritableDatabase();
                long id = ContentUris.parseId(uri);
                deletedRowCount = db.delete(DBManager.TBL_LOGIN_USER.NAME,
                        DBManager.TBL_LOGIN_USER._ID + "=?", new String[]{Long.toString(id)});
            }
            break;
            case KEY_DIR: {
                final SQLiteDatabase db = sDBManager.getWritableDatabase();
                long id = ContentUris.parseId(uri);
                deletedRowCount = db.delete(DBManager.TBL_KEY.NAME,
                        DBManager.TBL_KEY._ID + "=?", new String[]{Long.toString(id)});
            }
            break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deletedRowCount != 0) {
            // 컨텐트 리졸버에게 특정 URI 에 변경사항이 생겼다고 알린다.
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.d(TAG, "deletedRowCount = " + deletedRowCount);
        return deletedRowCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.i(TAG, "update(Uri uri, ContentValues values, String selection, String[] selectionArgs)");
        Log.d(TAG, "values = " + values);
        Log.d(TAG, "selection = " + selection);
        Log.d(TAG, "selectionArgs = " + selectionArgs);

        int updatedRowCount;
        int match = sUriMatcher.match(uri);

        switch (match) {
            case LOGIN_USER_DIR: {
                long id = ContentUris.parseId(uri);
                final SQLiteDatabase db = sDBManager.getWritableDatabase();
                updatedRowCount = db.update(DBManager.TBL_LOGIN_USER.NAME, values,
                        DBManager.TBL_LOGIN_USER._ID + "=?", new String[]{Long.toString(id)});
            }
            break;
            case KEY_DIR: {
                long id = ContentUris.parseId(uri);
                final SQLiteDatabase db = sDBManager.getWritableDatabase();
                updatedRowCount = db.update(DBManager.TBL_KEY.NAME, values,
                        DBManager.TBL_KEY._ID + "=?", new String[]{Long.toString(id)});
            }
            break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updatedRowCount != 0) {
            // 컨텐트 리졸버에게 특정 URI 에 변경사항이 생겼다고 알린다.
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.d(TAG, "updatedRowCount = " + updatedRowCount);
        return updatedRowCount;
    }

    private Uri insertOrUpdate(int match, Uri uri, ContentValues values) {
        String table = null;
        String column = null;
        switch (match) {
            case LOGIN_USER_DIR:
                table = DBManager.TBL_LOGIN_USER.NAME;
                column = DBManager.TBL_LOGIN_USER._ID;
                break;
            case KEY_DIR:
                table = DBManager.TBL_KEY.NAME;
                column = DBManager.TBL_KEY._ID;
                break;
        }

        // 새 데이터를 쓰기 위해 데이터베이스에 접근한다.
        final SQLiteDatabase db = sDBManager.getWritableDatabase();
        Uri returnUri = null;
        // insert or update
        Cursor cursor = db.query(table,
                null, null, null,null, null,
                column +" DESC");

        if(cursor == null || cursor.getCount() <= 0) {
            if(cursor != null) {
                cursor.close();
            }
            // insert
            long id = db.insert(table, null, values);
            if (id > 0) {
                // insert 성공
                // ContentUris 는 URI 를 쉽게 만들 수 있게 도와주는 메서드를 담고있는 헬퍼 클래스
                // withAppendId(): 첫 번째 인자를 베이스로 URI 를 만들고 두 번째 인자로 들어온 id를 path 의 마지막에 덧붙인다.
                returnUri = ContentUris.withAppendedId(uri, id);
            } else {
                // insert 실패
                throw new IllegalArgumentException("Failed to insert row into " + uri);
            }
        } else {
            // update
            if(cursor.moveToFirst()) {
                try {
                    try {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(column));
                        db.update(table, values,
                                column + "=?", new String[]{Long.toString(id)});
                        Log.e(TAG, "id = " + id);
                        returnUri = ContentUris.withAppendedId(uri, id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

}