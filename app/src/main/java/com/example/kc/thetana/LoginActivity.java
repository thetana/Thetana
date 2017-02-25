package com.example.kc.thetana;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by kc on 2017-02-14.
 */

public class LoginActivity extends AppCompatActivity {
    private EditText et_email;
    private EditText et_password;
    private Button bt_login;
    private Button bt_join;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_email = (EditText) findViewById(R.id.login_et_email);
        et_password = (EditText) findViewById(R.id.login_et_password);
        bt_login = (Button) findViewById(R.id.login_bt_login);
        bt_join = (Button) findViewById(R.id.login_bt_join);

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(et_email.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(et_password.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                getData(et_email.getText().toString(), et_password.getText().toString());
            }
        });
        bt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getData(String email, String password) {

        class GetDataJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(LoginActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String myJSON) {
                loading.dismiss();
                JSONObject jsonObj = null;
                String chk = "";
                String id = "";
                String name = "";
                String stateMessage = "";
                try {
                   jsonObj = new JSONObject(myJSON);
                   chk = jsonObj.getString("chk");
                   id = jsonObj.getString("id");
                   name = jsonObj.getString("name");
                   stateMessage = jsonObj.getString("stateMessage");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(chk.equals("success")){
                    preferences = getSharedPreferences("user", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("id", id);
                    editor.putString("name", name);
                    editor.putString("stateMessage", stateMessage);
                    editor.commit();

                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), chk, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String email = (String) params[0];
                    String password = (String) params[1];

                    String link = "http://jh-shin.synology.me/thetana/login.php";
                    String data = URLEncoder.encode("userEmail", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                    data += "&" + URLEncoder.encode("userPassword", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

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
                        sb.append(json);
                        break;
                    }

                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        GetDataJSON task = new GetDataJSON();
        task.execute(email, password);
    }
}
