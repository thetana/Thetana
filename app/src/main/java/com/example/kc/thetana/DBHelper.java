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
//        db.execSQL("CREATE TABLE friend (friendNo INTEGER PRIMARY KEY AUTOINCREMENT, friendId TEXT, bookmark TEXT, friendName TEXT, userName TEXT, stateMessage TEXT, phoneNumber TEXT, profilePicture TEXT, backgroundPhoto TEXT);");
//        db.execSQL("CREATE TABLE room (roomId INTEGER PRIMARY KEY, title TEXT, subTitle TEXT, roomGubun TEXT);");
//        db.execSQL("CREATE TABLE roommate (roommateId INTEGER PRIMARY KEY AUTOINCREMENT, roomId INTEGER, userId TEXT, userName TEXT, stateMessage TEXT, profilePicture TEXT, backgroundPhoto TEXT);");
//        db.execSQL("CREATE TABLE chat (chatId INTEGER PRIMARY KEY AUTOINCREMENT, chatNo INTEGER, roomId INTEGER, userId TEXT, gubun TEXT, message TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void edit(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }

    public JSONObject getFriends(String friendName) {
        SQLiteDatabase db = getReadableDatabase();

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        Cursor cursor = db.rawQuery("SELECT A.friendId, A.bookmark, IFNULL(NULLIF(A.friendName, ''), A.userName) AS friendName, A.stateMessage, A.phoneNumber, A.profilePicture, A.backgroundPhoto, IFNULL(B.roomId, '0') AS roomId FROM friend A LEFT JOIN roommate B ON A.friendId = B.userId AND B.roomId IN(SELECT roomId FROM room WHERE roomGubun = 'PtoP') WHERE A.friendName like '%" + friendName + "%'", null);
        int i = 0;
        try {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("friendId", cursor.getString(0));
                object.put("bookmark", cursor.getString(1));
                object.put("friendName", cursor.getString(2));
                object.put("stateMessage", cursor.getString(3));
                object.put("phoneNumber", cursor.getString(4));
                object.put("profilePicture", cursor.getString(5));
                object.put("backgroundPhoto", cursor.getString(6));
                object.put("roomId", cursor.getString(7));
                array.put(i, object);
                i++;
            }
            jsonObject.put("friend", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public JSONObject getInvite(String roomId) {
        SQLiteDatabase db = getReadableDatabase();

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        Cursor cursor = db.rawQuery("SELECT friendId, friendName FROM friend WHERE friendId NOT IN(SELECT userId FROM roommate WHERE roomId = " + roomId + ")", null);
        int i = 0;
        try {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("friendId", cursor.getString(0));
                object.put("friendName", cursor.getString(1));
                array.put(i, object);
                i++;
            }
            jsonObject.put("friend", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public JSONObject getRoom(String title) {
        SQLiteDatabase db = getReadableDatabase();

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        Cursor cursor = db.rawQuery("SELECT roomId, subTitle, roomGubun FROM room", null);
        int i = 0;
        try {
            while (cursor.moveToNext()) {
                Cursor cursor1 = db.rawQuery("SELECT IFNULL(NULLIF(B.friendName, ''), A.userName) AS name FROM roommate A LEFT JOIN friend B ON A.userId = B.friendId WHERE A.roomId = " + cursor.getString(0), null);
                String roomName = "";
                while (cursor1.moveToNext()) {
                    roomName = roomName + "," + cursor1.getString(0);
                }
                roomName = roomName.substring(1);

                JSONObject object = new JSONObject();
                object.put("roomId", cursor.getString(0));
                object.put("subTitle", cursor.getString(1));
                object.put("roomGubun", cursor.getString(2));
                object.put("roomName", roomName);
                array.put(i, object);
                i++;
            }
            jsonObject.put("room", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public JSONObject getRoommate(String roomId) {
        SQLiteDatabase db = getReadableDatabase();

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        Cursor cursor = db.rawQuery("SELECT userId, IFNULL(NULLIF(B.friendName, ''), A.userName) AS userName, A.stateMessage, A.profilePicture, A.backgroundPhoto FROM roommate A LEFT JOIN friend B ON A.userId = B.friendId WHERE A.roomId = " + roomId, null);
        int i = 0;
        try {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("userId", cursor.getString(0));
                object.put("userName", cursor.getString(1));
                object.put("stateMessage", cursor.getString(2));
                object.put("profilePicture", cursor.getString(3));
                object.put("backgroundPhoto", cursor.getString(4));
                array.put(i, object);
                i++;
            }
            jsonObject.put("roommate", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public JSONObject getChat(String roomId) {
        SQLiteDatabase db = getReadableDatabase();

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        Cursor cursor = db.rawQuery("SELECT A.chatNo, A.roomId, A.userId, A.gubun, A.message, B.friendName FROM chat A LEFT JOIN friend B ON A.userId = B.friendId WHERE roomId = '" + roomId + "'", null);
        int i = 0;
        try {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("chatNo", cursor.getInt(0));
                object.put("roomId", cursor.getInt(1));
                object.put("userId", cursor.getString(2));
                object.put("gubun", cursor.getString(3));
                object.put("message", cursor.getString(4));
                object.put("friendName", cursor.getInt(5));
                array.put(i, object);
                i++;
            }
            jsonObject.put("chat", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public String getChatNo(String roomId) {
        String chatNo = "0";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(chatNo, 0) AS chatNo FROM room WHERE roomId = " + roomId, null);

        while (cursor.moveToNext()) {
            chatNo = cursor.getString(0);
        }
        return chatNo;
    }

    public int chatCount(String chatId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT count(*) As chatId FROM chat where chatId = '" + chatId + "'", null);

        return cursor.getInt(0);
    }

    public String getRoommateName(String userId) {
        String userName = "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(NULLIF(B.friendName, ''), A.userName) AS userName FROM roommate A LEFT JOIN friend B ON A.userId = B.friendId WHERE A.userId = '" + userId + "'", null);

        while (cursor.moveToNext()) {
            userName = cursor.getString(0);
        }
        return userName;
    }

    public JSONObject getFriend(String userId) {
        String userName = "";
        SQLiteDatabase db = getReadableDatabase();
        JSONObject jsonObject = new JSONObject();
        Cursor cursor = db.rawQuery("SELECT IFNULL(NULLIF(friendName, ''), userName) AS friendName, userName, stateMessage, phoneNumber, profilePicture, backgroundPhoto FROM friend WHERE friendId = '" + userId + "'", null);
        while (cursor.moveToNext()) {
            try {
                jsonObject.put("friendName", cursor.getString(0));
                jsonObject.put("userName", cursor.getString(1));
                jsonObject.put("stateMessage", cursor.getString(2));
                jsonObject.put("phoneNumber", cursor.getString(3));
                jsonObject.put("profilePicture", cursor.getString(4));
                jsonObject.put("backgroundPhoto", cursor.getString(5));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}
