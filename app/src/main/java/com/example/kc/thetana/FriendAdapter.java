package com.example.kc.thetana;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kc on 2017-02-18.
 */

public class FriendAdapter extends BaseAdapter {

    private ArrayList<FriendItem> friendItems = new ArrayList<FriendItem>();
    TextView tv_name;
    Button bt_add;
    SharedPreferences preferences;

    public void addItem(ArrayList<FriendItem> itme) {
        friendItems = itme;
        notifyDataSetChanged();
    }

    public void addItem(FriendItem itme) {
        FriendItem friendItem = itme;

        friendItems.add(friendItem);
        notifyDataSetChanged();
    }

    public void clearItem() {
        friendItems.clear();
    }

    @Override
    public int getCount() {
        return friendItems.size();
    }

    @Override
    public Object getItem(int position) {
        return friendItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_add_friend, parent, false);

        tv_name = (TextView) convertView.findViewById(R.id.addFriend_tv_name);
        bt_add = (Button) convertView.findViewById(R.id.addFriend_bt_add);
        tv_name.setText(friendItems.get(position).name);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertToDatabase(parent.getContext(), position, friendItems.get(position).id);
            }
        });


        return convertView;
    }

    private void insertToDatabase(final Context context, final int position, String id) {

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            DBHelper dbHelper = new DBHelper(context, "thetana.db", null, 1);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(context, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();

                JSONObject jsonObject = null;
                JSONObject object = null;
                JSONArray jsonArray = null;

                try {
                    jsonObject = new JSONObject(s);
                    if (jsonObject.getString("success").equals("success")) {
                        jsonArray = new JSONArray(jsonObject.getString("friend"));
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

                        friendItems.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, jsonObject.getString("success"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    preferences = context.getSharedPreferences("user", 0);

                    String userId = preferences.getString("id", "");
                    String friendId = (String) params[0];

                    String link = context.getString(R.string.ip) + "addFriend.php";
                    String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    data += "&" + URLEncoder.encode("friendId", "UTF-8") + "=" + URLEncoder.encode(friendId, "UTF-8");

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
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(id);
    }
}
