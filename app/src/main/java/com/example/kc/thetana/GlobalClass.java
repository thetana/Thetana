package com.example.kc.thetana;

import android.app.ProgressDialog;
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
import java.security.PrivateKey;
import java.util.ArrayList;

/**
 * Created by kc on 2017-02-19.
 */

public class GlobalClass {
    private ArrayList<FriendGroup> friendGroup = new ArrayList<FriendGroup>();
    private Context context;
    private SharedPreferences preferences;

    GlobalClass(Context context) {
        this.context = context;
    }

    public void updateFriends() {
        preferences = context.getSharedPreferences("user", 0);
        getData(preferences.getString("id", ""));
    }

    public ArrayList<FriendGroup> getFriends() {
        JSONArray jsonArray = null;

        friendGroup.add(new FriendGroup("프로필"));
        friendGroup.add(new FriendGroup("즐겨찾기"));
        friendGroup.add(new FriendGroup("친구"));

        preferences = context.getSharedPreferences("user", 0);

        friendGroup.get(0).friendChildren.add(new FriendChild());
        friendGroup.get(0).friendChildren.get(0).id = preferences.getString("id", "");
        friendGroup.get(0).friendChildren.get(0).name = preferences.getString("name", "");
        friendGroup.get(0).friendChildren.get(0).state = preferences.getString("stateMessage", "");

        try {
            JSONObject jsonObj = new JSONObject(preferences.getString("friend", ""));
            jsonArray = jsonObj.getJSONArray("friend");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);
                String friendId = c.getString("friendId");
                String bookmark = c.getString("bookmark");
                String userName = c.getString("userName");
                String stateMessage = c.getString("stateMessage");

                FriendChild friendChild = new FriendChild();
                friendChild.id = friendId;
                friendChild.name = userName;
                friendChild.state = stateMessage;

                friendGroup.get(2).friendChildren.add(friendChild);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return friendGroup;
    }

    private void getData(String id) {

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(context, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();

                preferences = context.getSharedPreferences("user", 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("friend", s);
                editor.commit();
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    String id = (String) params[0];

                    String link = "http://jh-shin.synology.me/thetana/getMyFriend.php";
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
        task.execute(id);
    }
}
