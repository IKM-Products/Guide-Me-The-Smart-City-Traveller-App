package com.skm.guideme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "guide_me.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table for place summaries
        db.execSQL("CREATE TABLE PlaceSummary (" +
                "place_name TEXT PRIMARY KEY, " +
                "summary TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old table if exists and recreate
        db.execSQL("DROP TABLE IF EXISTS PlaceSummary");
        onCreate(db);
    }

    // Insert or update summary for a place
    public void insertOrUpdateSummary(String placeName, String summary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("place_name", placeName);
        values.put("summary", summary);
        db.insertWithOnConflict("PlaceSummary", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Get summary for a place
    public String getSummary(String placeName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT summary FROM PlaceSummary WHERE place_name = ?", new String[]{placeName});
        String summary = null;
        if (cursor.moveToFirst()) {
            summary = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return summary;
    }
}
