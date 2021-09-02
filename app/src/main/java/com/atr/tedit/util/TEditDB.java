/*
 * Free Public License 1.0.0
 * Permission to use, copy, modify, and/or distribute this software
 * for any purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.atr.tedit.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.atr.tedit.settings.TxtSettings;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class TEditDB {
    public static final String KEY_PATH = "path";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATA = "data";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "TEditDB";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_CREATE =
            "create table texts (_id integer primary key autoincrement, "
                    + "path text not null, body text not null, data blob not null);";

    private static final String DATABASE_NAME = "tedit_data";
    private static final String DATABASE_TABLE = "texts";
    private static final int DATABASE_VERSION = 5;

    private final Context ctx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public TEditDB(Context ctx) {
        this.ctx = ctx;
    }

    public TEditDB open() throws SQLException {
        mDbHelper = new DatabaseHelper(ctx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createText(String title, String body) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PATH, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATA, new TxtSettings().toByteArray());

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteText(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean deleteAll() {
        return mDb.delete(DATABASE_TABLE, "1", null) > 0;
    }

    public Cursor fetchAllTexts() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_PATH, KEY_BODY, KEY_DATA}, null, null,
                        null, null, null);
    }

    public long hasFile(String path) {
        Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_PATH, KEY_BODY},
                KEY_PATH + "=" + path, null,
                null, null, null, null);
        if (cursor == null || cursor.getColumnIndex(KEY_ROWID) ==  -1)
            return -1;

        return cursor.getLong(cursor.getColumnIndex(KEY_ROWID));
    }

    public Cursor fetchText(long rowId) throws SQLException {
        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_PATH, KEY_BODY, KEY_DATA},
                        KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor.getCount() == 0) {
            mCursor.close();
            return null;
        }

        if (mCursor != null)
            mCursor.moveToFirst();

        return mCursor;
    }

    public boolean updateText(long rowId, String path, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_PATH, path);
        args.put(KEY_BODY, body);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateTextState(long rowId, TxtSettings settings) {
        ContentValues args = new ContentValues();
        args.put(KEY_DATA, settings.toByteArray());

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
