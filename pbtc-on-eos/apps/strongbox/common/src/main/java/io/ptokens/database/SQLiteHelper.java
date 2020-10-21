package io.ptokens.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.AbstractWindowedCursor;
import android.database.CursorWindow;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import org.sqlite.database.sqlite.SQLiteCursor;
import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import io.ptokens.utils.Operations;

public class SQLiteHelper extends SQLiteOpenHelper {
    private final static String TAG = SQLiteHelper.class.getName();
    private final static String DATABASE_NAME = "StrongboxDatabase";
    private final static int DATABASE_VERSION = 2;
    private final static String CURSOR_WINDOW_NAME = "ValueWindow";
    private final static int CURSOR_WINDOW_MAX_BYTES = 80000000;

    public SQLiteHelper(Context context) {
        super(
                context,
                context.getDatabasePath(DATABASE_NAME).getAbsolutePath(),
                null,
                DATABASE_VERSION
        );
        Log.d(TAG, "Database path "
                + context.getDatabasePath(DATABASE_NAME).getAbsolutePath()
        );
    }

    public static void copyDatabaseToSdCard(Context context) {
        File src = context.getDatabasePath(DATABASE_NAME);
        Log.d(TAG, "Copying " + DATABASE_NAME + " to /sdcard...");
        File dest = Paths.get(
                Environment.getExternalStorageDirectory()
                        .getPath(),
                DATABASE_NAME
        ).toFile();

        try {
            Operations.copy(src, dest);
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy the database to " + dest.getPath(), e);
        }
    }

    /**
     * Place the database to import inside the sdcard with
     * the expected DATABASE_NAME
     * @param context
     */
    public static void copyDatabaseFromSdCard(Context context) {

        File src = Paths.get(
                Environment.getExternalStorageDirectory()
                        .getPath(),
                DATABASE_NAME
        ).toFile();
        File dest = context.getDatabasePath(DATABASE_NAME);
        Log.d(TAG, "Copying " + DATABASE_NAME + " from /sdcard/ " + DATABASE_NAME);
        String parent = context.getDatabasePath(DATABASE_NAME).getParent();
        File backupDest = Paths.get(parent, DATABASE_NAME  + "-backup").toFile();

        try {
            Operations.copy(dest, backupDest);
        } catch (IOException e) {
            Log.e(TAG, "Failed to backup the current databases", e);
        }
        try {

            Operations.copy(src, dest);
        } catch (IOException e) {
            Log.e(TAG, "Failed to import the database from /sdcard" + dest.getPath(), e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.DatabaseEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.DatabaseEntry.SQL_UPGRADE_DATABASE);
        onCreate(db);
    }

    static void insertOrReplace(SQLiteDatabase db, String key, byte[] value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.DatabaseEntry.FIELD_KEY, key);
        contentValues.put(DatabaseContract.DatabaseEntry.FIELD_VALUE, value);

        db.replace(DatabaseContract.DatabaseEntry.TABLE_NAME, null, contentValues);
    }

    static void loadExtension(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.DatabaseEntry.SQL_LOAD_EXTENSION);
        Log.d(TAG, "✔ Extension loaded");
    }

    static byte[] getBytesFromKey(SQLiteDatabase db, String key) {
        String[] columns = { DatabaseContract.DatabaseEntry.FIELD_VALUE };
        String selection = DatabaseContract.DatabaseEntry.FIELD_KEY + " = ?";
        String[] selectionArgs = { key };

        CursorWindow cursorWindow = new CursorWindow(
                CURSOR_WINDOW_NAME,
                CURSOR_WINDOW_MAX_BYTES
        );

        try(SQLiteCursor cursor = (SQLiteCursor) db.query(
                DatabaseContract.DatabaseEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        )) {
            AbstractWindowedCursor aWindowedCursor = (AbstractWindowedCursor) cursor;
            aWindowedCursor.setWindow(cursorWindow);
            aWindowedCursor.moveToNext();
            return cursor.getBlob(0);
        }
    }

    static ArrayList<Pair<String, byte[]>> getKeysAndHashedValues(SQLiteDatabase db) {
        CursorWindow cursorWindow= new CursorWindow(
                CURSOR_WINDOW_NAME,
                CURSOR_WINDOW_MAX_BYTES
        );

        try(SQLiteCursor cursor = (SQLiteCursor) db.rawQuery(
                DatabaseContract.DatabaseEntry.SQL_GET_ALL_AND_SHA3_VALUES,
                null)
        ) {

            AbstractWindowedCursor aWindowedCursor = cursor;
            aWindowedCursor.setWindow(cursorWindow);

            ArrayList<Pair<String, byte[]>> keyValuePairs = new ArrayList<>();
            while(aWindowedCursor.moveToNext()) {
                keyValuePairs.add(new Pair<>(
                        aWindowedCursor.getString(0),
                        aWindowedCursor.getBlob(1))
                );
            }
            return keyValuePairs;
        }
    }

    static void deleteKey(SQLiteDatabase db, String key) {
        String selection = DatabaseContract.DatabaseEntry.FIELD_KEY + " = ?";
        String[] selectionArgs = { key };

        int n = db.delete(DatabaseContract.DatabaseEntry.TABLE_NAME,selection, selectionArgs);
        Log.d(TAG,  n + " records removed");
    }
}
