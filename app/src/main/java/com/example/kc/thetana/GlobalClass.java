package com.example.kc.thetana;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by kc on 2017-02-19.
 */

public class GlobalClass {
    private ArrayList<FriendGroup> friendGroup = new ArrayList<FriendGroup>();
    private ArrayList<RoomItem> roomItems = new ArrayList<RoomItem>();
    private Context context;
    private SharedPreferences preferences;
    String myId;

    GlobalClass(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("user", 0);
        myId = preferences.getString("id", "");
    }

    public void updateFriends() {
        preferences = context.getSharedPreferences("user", 0);
        getData(myId, context.getString(R.string.ip) + "getMyFriend.php");
    }

    public ArrayList<FriendGroup> getFriends() {
        friendGroup.add(new FriendGroup("프로필"));
        friendGroup.add(new FriendGroup("즐겨찾기"));
        friendGroup.add(new FriendGroup("친구"));

        preferences = context.getSharedPreferences("user", 0);
        friendGroup.get(0).friendChildren.add(new FriendChild());
        friendGroup.get(0).friendChildren.get(0).id = preferences.getString("id", "");
        friendGroup.get(0).friendChildren.get(0).name = preferences.getString("name", "");
        friendGroup.get(0).friendChildren.get(0).state = preferences.getString("stateMessage", "");

        preferences = context.getSharedPreferences("friend", 0);
        Iterator<String> iterator = preferences.getAll().keySet().iterator();
        while (iterator.hasNext()) {
            try {
                JSONObject c = new JSONObject(preferences.getString(iterator.next(), ""));
                String friendId = c.getString("friendId");
                String bookmark = c.getString("bookmark");
                String userName = c.getString("userName");
                String stateMessage = c.getString("stateMessage");
                String roomId = c.getString("roomId");

                FriendChild friendChild = new FriendChild();
                friendChild.id = friendId;
                friendChild.name = userName;
                friendChild.state = stateMessage;
                friendChild.roomId = roomId;

                friendGroup.get(2).friendChildren.add(friendChild);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return friendGroup;
    }

    public void updateRooms() {
        preferences = context.getSharedPreferences("user", 0);
        getData(myId, context.getString(R.string.ip) + "getRoom.php");
    }

    public ArrayList<RoomItem> getRooms() {

        preferences = context.getSharedPreferences("room", 0);
        Iterator<String> iterator = preferences.getAll().keySet().iterator();
        while (iterator.hasNext()) {
            try {
                JSONObject jsonObject = new JSONObject(preferences.getString(iterator.next(), ""));
                String roomId = jsonObject.getString("roomId");
                String roomGubun = jsonObject.getString("roomGubun");
                String roomName = "";

                JSONArray jsonArray = jsonObject.getJSONArray("roomName");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);

                    JSONObject friend = new JSONObject(context.getSharedPreferences("friend", 0).getString(c.getString("userId"), ""));
                    String name = friend.getString("userName");

                    roomName = roomName + "," + name;
                }
                roomName = roomName.substring(1);

                RoomItem roomItem = new RoomItem();
                roomItem.id = roomId;
                roomItem.gubun = roomGubun;
                roomItem.name = roomName;

                roomItems.add(roomItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return roomItems;
    }

    private void getData(final String id, final String link) {

        class InsertData extends AsyncTask<String, Void, String> {
//            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
//                loading = ProgressDialog.show(context, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                loading.dismiss();
                if (link.equals(context.getString(R.string.ip) + "getMyFriend.php")) {
                    try {
                        preferences = context.getSharedPreferences("friend", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();

                        JSONObject jsonObj = null;
                        jsonObj = new JSONObject(s);
                        JSONArray jsonArray = jsonObj.getJSONArray("friend");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject c = jsonArray.getJSONObject(i);
                            editor.putString(c.getString("friendId"), c.toString());
                        }

                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (link.equals(context.getString(R.string.ip) + "getRoom.php")) {
                    try {
                        preferences = context.getSharedPreferences("room", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();

                        JSONObject jsonObj = null;
                        jsonObj = new JSONObject(s);
                        JSONArray jsonArray = jsonObj.getJSONArray("room");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject c = jsonArray.getJSONObject(i);
                            editor.putString(c.getString("roomId"), c.toString());
                        }

                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    String id = (String) params[0];
                    String link = (String) params[1];

                    String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String json;
                    while ((json = reader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    return sb.toString().trim();

                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }

        InsertData task = new InsertData();
        task.execute(id, link);
    }
}
