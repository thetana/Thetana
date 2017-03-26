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
        db.execSQL("CREATE TABLE chat (chatId INTEGER PRIMARY KEY, chatNo INTEGER, roomId INTEGER, userId TEXT, gubun TEXT, readed INTEGER, gubun TEXT, insertDt TEXT, updateDt TEXT);");
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

        Cursor cursor = db.rawQuery("SELECT chatId, chatNo, roomId, userId, gubun, message, readed, insertDt, updateDt FROM chat where roomId = '" + roomId + "'", null);
        int i = 0;
        try {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("chatId", cursor.getInt(0));
                object.put("chatNo", cursor.getInt(1));
                object.put("roomId", cursor.getInt(2));
                object.put("userId", cursor.getString(3));
                object.put("gubun", cursor.getString(4));
                object.put("message", cursor.getString(5));
                object.put("readed", cursor.getInt(6));
                object.put("insertDt", cursor.getString(7));
                object.put("updateDt", cursor.getString(8));
                array.put(i, object);
                i++;
            }
            jsonObject.put("chat", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
    public int chatCount(String chatId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT count(*) As chatId FROM chat where chatId = '" + chatId + "'", null);

        return cursor.getInt(0);
    }
}
