package com.example.kc.thetana;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by kc on 2017-02-19.
 */

public class GlobalClass {
    ArrayList<FriendGroup> friendGroup = new ArrayList<FriendGroup>();
    Context context;
    SharedPreferences preferences;

    GlobalClass(Context context) {
        this.context = context;
    }

    public void updateFriends() {
        preferences = context.getSharedPreferences("user", 0);
        getData(preferences.getString("id", ""));
    }

    public ArrayList<FriendGroup> getFriends() {
        friendGroup.add(new FriendGroup("프로필"));
        friendGroup.add(new FriendGroup("즐겨찾기"));
        friendGroup.add(new FriendGroup("친구"));

        preferences = context.getSharedPreferences("user", 0);
        getData(preferences.getString("friend", ""));



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
