package com.example.kc.thetana;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by kc on 2017-02-18.
 */

public class FriendActivity extends AppCompatActivity {
    EditText et_id;
    Button bt_serch;
    ListView lv_friend;
    FriendAdapter adapter;
    SharedPreferences preferences;
    private ArrayList<FriendItem> friendItems = new ArrayList<FriendItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        et_id = (EditText) findViewById(R.id.friend_et_id);
        bt_serch = (Button) findViewById(R.id.friend_bt_serch);
        lv_friend = (ListView) findViewById(R.id.friend_lv_friend);
        adapter = new FriendAdapter();
        lv_friend.setAdapter(adapter);

        bt_serch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(et_id.getText().toString());
            }
        });
    }

    public void getData(String id) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                try {
                    preferences = getSharedPreferences("user", 0);

                    String userId = preferences.getString("id", "");
                    String friendId = (String) params[0];

                    String link = getString(R.string.ip) + "getFriend.php";
                    String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    data += "&" + URLEncoder.encode("friendId", "UTF-8") + "=" + URLEncoder.encode(friendId, "UTF-8");

                    URL url = new URL(link);
                    URLConnection con = url.openConnection();

                    con.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    return sb.toString().trim();

                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray friends = jsonObj.getJSONArray("result");

                    ArrayList<FriendItem> friendItems = new ArrayList<FriendItem>();
                    adapter.clearItem();
                    for (int i = 0; i < friends.length(); i++) {
                        JSONObject c = friends.getJSONObject(i);
                        FriendItem friendItem = new FriendItem();

                        friendItem.id = c.getString("userId");
                        friendItem.name = c.getString("userName");

                        friendItems.add(friendItem);
//                        adapter.addItem(friendItem);
                    }
                    adapter.addItem(friendItems);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(id);
    }

}
