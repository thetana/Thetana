package com.example.kc.thetana;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    SharedPreferences preferences;
    DBHelper dbHelper = new DBHelper(MainActivity.this, "thetana.db", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("user", 0);
        final String id = preferences.getString("id", "");

        if (TextUtils.isEmpty(id) || id.equals(null) || id.equals("null")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            class GetDataJSON extends AsyncTask<String, Void, String> {
                ProgressDialog loading;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
                }

                @Override
                protected void onPostExecute(String myJSON) {
                    loading.dismiss();
                    JSONObject jsonObject = null;
                    JSONObject object = null;
                    JSONArray jsonArray = null;
                    try {
                        jsonObject = new JSONObject(myJSON);
                        jsonArray = new JSONArray(jsonObject.getString("friend"));
                        dbHelper.edit("DELETE FROM friend;");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            object = jsonArray.getJSONObject(i);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSERT INTO friend VALUES(null, '");
                            stringBuilder.append(object.getString("friendId")).append("', '");
                            stringBuilder.append(object.getString("bookmark")).append("', '");
                            stringBuilder.append(object.getString("friendName")).append("', '");
                            stringBuilder.append(object.getString("userName")).append("', '");
                            stringBuilder.append(object.getString("stateMessage")).append("', '");
                            stringBuilder.append(object.getString("phoneNumber")).append("', '");
                            stringBuilder.append(object.getString("profilePicture")).append("', '");
                            stringBuilder.append(object.getString("backgroundPhoto")).append("')");
                            dbHelper.edit(stringBuilder.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

                @Override
                protected String doInBackground(String... params) {
                    try {
                        String link = getString(R.string.ip) + "getMyFriend.php";
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
            GetDataJSON task = new GetDataJSON();
            task.execute();
        }
    }
}
