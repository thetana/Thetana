package com.example.kc.thetana;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by kc on 2017-02-14.
 */

public class JoinActivity extends AppCompatActivity {

    private EditText et_email;
    private EditText et_password;
    private EditText et_rePassword;
    private EditText et_id;
    private EditText et_name;
    private Button bt_join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);


        et_email = (EditText) findViewById(R.id.join_et_email);
        et_password = (EditText) findViewById(R.id.join_et_password);
        et_rePassword = (EditText) findViewById(R.id.join_et_rePassword);
        et_id = (EditText) findViewById(R.id.join_et_id);
        et_name = (EditText) findViewById(R.id.join_et_name);
        bt_join = (Button) findViewById(R.id.join_bt_join);


        bt_join.setOnClickListener(new View.OnClickListener() {
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
                if(TextUtils.isEmpty(et_rePassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 재입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(et_id.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(et_name.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                insertToDatabase(et_email.getText().toString()
                        , et_password.getText().toString()
                        , et_rePassword.getText().toString()
                        , et_id.getText().toString()
                        , et_name.getText().toString());
            }
        });
    }


    private void insertToDatabase(String email, String password, String rePassword, String id, String name) {

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(JoinActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                if (s.equals("환영합니다.")) {
                    Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    String email = (String) params[0];
                    String password = (String) params[1];
                    String rePassword = (String) params[2];
                    String id = (String) params[3];
                    String name = (String) params[4];

                    String link = "http://192.168.244.128/join.php";
                    String data = URLEncoder.encode("userEmail", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                    data += "&" + URLEncoder.encode("userPassword", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                    data += "&" + URLEncoder.encode("rePassword", "UTF-8") + "=" + URLEncoder.encode(rePassword, "UTF-8");
                    data += "&" + URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    data += "&" + URLEncoder.encode("userName", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");

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
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(email, password, rePassword, id, name);
    }

}
