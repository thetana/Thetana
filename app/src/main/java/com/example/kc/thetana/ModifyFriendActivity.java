package com.example.kc.thetana;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class ModifyFriendActivity extends AppCompatActivity {
    TextView tb_title;
    EditText et_before, et_after;
    ImageButton ib_get;
    DBHelper dbHelper = new DBHelper(ModifyFriendActivity.this, "thetana.db", null, 1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_friend);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionbar = inflater.inflate(R.layout.actionbar_make, null);
        actionBar.setCustomView(actionbar);
        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar)actionbar.getParent();
        parent.setContentInsetsAbsolute(0,0);
        actionBar.setElevation(0); // 그림자 없애기

        tb_title = (TextView) actionbar.findViewById(R.id.tb_title);
        tb_title.setText("프로필");

        ib_get = (ImageButton) actionbar.findViewById(R.id.ib_get);
        ib_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String friendName = et_after.getText().toString();
                class InsertData extends AsyncTask<Object, Object, Void> {
                    ProgressDialog loading;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        loading = ProgressDialog.show(ModifyFriendActivity.this, "Please Wait", null, true, true);
                    }

                    @Override
                    protected void onPostExecute(Void s) {
                        super.onPostExecute(s);
                        loading.dismiss();
                        finish();
                    }

                    @Override
                    protected Void doInBackground(Object... params) {
                        try {
                            String link = getString(R.string.ip) + "putFriendName.php";
                            String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(getSharedPreferences("user", 0).getString("id", ""), "UTF-8");
                            data += "&" + URLEncoder.encode("friendId", "UTF-8") + "=" + URLEncoder.encode(getIntent().getStringExtra("id"), "UTF-8");
                            data += "&" + URLEncoder.encode("friendName", "UTF-8") + "=" + URLEncoder.encode(friendName, "UTF-8");

                            URL url = new URL(link);
                            URLConnection conn = url.openConnection();

                            conn.setDoOutput(true);
                            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                            wr.write(data);
                            wr.flush();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                            StringBuilder sb = new StringBuilder();
                            String line = null;

                            // Read Server Response
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                                break;
                            }
                        } catch (Exception e) {
                            e.toString();
                        }
                        return null;
                    }
                }
                InsertData task = new InsertData();
                task.execute();

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("UPDATE friend SET friendName = '");
                stringBuilder.append(friendName).append("' WHERE friendId = '");
                stringBuilder.append(getIntent().getStringExtra("id")).append("'");
                dbHelper.edit(stringBuilder.toString());
            }
        });


        et_before = (EditText) findViewById(R.id.modify_f_et_before);
        et_after = (EditText) findViewById(R.id.modify_f_et_after);
        JSONObject jsonObject = dbHelper.getFriend(getIntent().getStringExtra("id"));
        try {
            et_before.setText(jsonObject.getString("userName"));
            et_after.setText(jsonObject.getString("friendName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
