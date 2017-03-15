package com.example.kc.thetana;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kc on 2017-03-12.
 */

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE chat (chatId INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT, userId TEXT, roomId TEXT, gubun TEXT, insertDt TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void edit(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }

    public JSONObject getChat(String roomId) {
        SQLiteDatabase db = getReadableDatabase();

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        Cursor cursor = db.rawQuery("SELECT userId, message FROM chat where roomId = '" + roomId + "'", null);
        int i = 0;
        try {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("userId", cursor.getString(0));
                object.put("message", cursor.getString(1));
                array.put(i, object);
                i++;
            }
            jsonObject.put("chat", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
