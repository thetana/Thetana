package com.example.kc.thetana;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
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

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private EditText et_email;
    private EditText et_password;
    private ImageButton bt_login;
    private ImageButton bt_join;
    SharedPreferences preferences;
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";
    String token, before;
    DBHelper dbHelper = new DBHelper(this, "thetana.db", null, 1);

    JSONObject jsonObject = null;
    JSONObject object = null;
    JSONArray jsonArray = null;
    String chk = "";
    String id = "";
    String userEmail = "";
    String name = "";
    String stateMessage = "";
    String phoneNumber = "";
    String profilePicture = "";
    String backgroundPhoto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
//                .requestIdToken(getString(R.string.server_client_id))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        et_email = (EditText) findViewById(R.id.login_et_email);
        et_password = (EditText) findViewById(R.id.login_et_password);
        bt_login = (ImageButton) findViewById(R.id.login_bt_login);
        bt_join = (ImageButton) findViewById(R.id.login_bt_join);

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_email.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et_password.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                getData(LoginActivity.this, et_email.getText().toString(), et_password.getText().toString());
            }
        });
        bt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });
        if (getIntent().getStringExtra("killed").equals("killed"))
            Toast.makeText(getApplicationContext(), "다른 핸드폰에 로그인 되어 로그아웃 합니다.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        Log.d(TAG, result.getStatus().toString());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            String Id = acct.getId();
            String idToken = acct.getIdToken();

            System.out.print("personName : " + personName);
            System.out.print("personGivenName : " + personGivenName);
            System.out.print("personFamilyName : " + personFamilyName);
            System.out.print("personEmail : " + personEmail);
            System.out.print("personId : " + personId);
            System.out.print("Id : " + Id);
            System.out.print("idToken : " + idToken);
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    private void getData(final Context context, String email, String password) {
        token = FirebaseInstanceId.getInstance().getToken();

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
                jsonObject = null;
                object = null;
                jsonArray = null;
                chk = "";
                id = "";
                userEmail = "";
                name = "";
                stateMessage = "";
                phoneNumber = "";
                profilePicture = "";
                backgroundPhoto = "";
                before = "";

                try {
                    jsonObject = new JSONObject(myJSON);
                    object = new JSONObject(jsonObject.getString("me"));

                    chk = object.getString("chk");
                    id = object.getString("id");
                    userEmail = object.getString("userEmail");
                    name = object.getString("name");
                    stateMessage = object.getString("stateMessage");
                    phoneNumber = object.getString("phoneNumber");
                    profilePicture = object.getString("profilePicture");
                    backgroundPhoto = object.getString("backgroundPhoto");
                    before = object.getString("fcmToken");
                    if (chk.equals("success") && !object.getString("fcmToken").equals("")) {
                        chk = "";
                        AlertDialog.Builder alertDlg = new AlertDialog.Builder(LoginActivity.this);
                        alertDlg.setTitle("이미 다른 핸드폰에 로그인 중 입니다.");
                        alertDlg.setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                putToken(before, token);
                                preferences = getSharedPreferences("user", 0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("id", id);
                                editor.putString("Email", userEmail);
                                editor.putString("name", name);
                                editor.putString("stateMessage", stateMessage);
                                editor.putString("phoneNumber", phoneNumber);
                                editor.putString("profilePicture", profilePicture);
                                editor.putString("backgroundPhoto", backgroundPhoto);
                                editor.putString("token", token);
                                editor.commit();

                                dbHelper.edit("CREATE TABLE friend (friendNo INTEGER PRIMARY KEY AUTOINCREMENT, friendId TEXT, bookmark TEXT, friendName TEXT, userName TEXT, stateMessage TEXT, phoneNumber TEXT, profilePicture TEXT, backgroundPhoto TEXT);");
                                dbHelper.edit("CREATE TABLE room (roomId INTEGER PRIMARY KEY, title TEXT, subTitle TEXT, roomGubun TEXT);");
                                dbHelper.edit("CREATE TABLE roommate (roommateId INTEGER PRIMARY KEY AUTOINCREMENT, roomId INTEGER, userId TEXT, userName TEXT, stateMessage TEXT, profilePicture TEXT, backgroundPhoto TEXT, chatNo INTEGER);");
                                dbHelper.edit("CREATE TABLE chat (chatId INTEGER PRIMARY KEY AUTOINCREMENT, chatNo INTEGER, roomId INTEGER, userId TEXT, gubun TEXT, message TEXT, dtTm TEXT);");
                                dbHelper.edit("CREATE TABLE tempChat (chatId INTEGER PRIMARY KEY AUTOINCREMENT, roomId INTEGER, userId TEXT, gubun TEXT, message TEXT);");

                                try {
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

                                    jsonArray = new JSONArray(jsonObject.getString("room"));
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        object = jsonArray.getJSONObject(i);
                                        StringBuilder stringBuilder = new StringBuilder();
                                        stringBuilder.append("INSERT INTO room VALUES(").append(object.getString("roomId")).append(", '");
                                        stringBuilder.append(object.getString("title")).append("', '");
                                        stringBuilder.append(object.getString("subTitle")).append("', '");
                                        stringBuilder.append(object.getString("roomGubun")).append("')");
                                        dbHelper.edit(stringBuilder.toString());
                                    }

                                    jsonArray = new JSONArray(jsonObject.getString("roommate"));
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        object = jsonArray.getJSONObject(i);
                                        StringBuilder stringBuilder = new StringBuilder();
                                        stringBuilder.append("INSERT INTO roommate VALUES(null, ");
                                        stringBuilder.append(object.getString("roomId")).append(", '");
                                        stringBuilder.append(object.getString("userId")).append("', '");
                                        stringBuilder.append(object.getString("userName")).append("', '");
                                        stringBuilder.append(object.getString("stateMessage")).append("', '");
                                        stringBuilder.append(object.getString("profilePicture")).append("', '");
                                        stringBuilder.append(object.getString("backgroundPhoto")).append("', '");
                                        stringBuilder.append(object.getString("chatNo")).append("')");
                                        dbHelper.edit(stringBuilder.toString());

                                    }
                                    jsonArray = new JSONArray(jsonObject.getString("chat"));
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        object = jsonArray.getJSONObject(i);
                                        StringBuilder stringBuilder = new StringBuilder();
                                        stringBuilder.append("INSERT INTO chat VALUES(null, ");
                                        stringBuilder.append(object.getString("chatNo")).append(", ");
                                        stringBuilder.append(object.getString("roomId")).append(", '");
                                        stringBuilder.append(object.getString("userId")).append("', '");
                                        stringBuilder.append(object.getString("gubun")).append("', '");
                                        stringBuilder.append(object.getString("message")).append("', '");
                                        stringBuilder.append(object.getString("dtTm")).append("')");
                                        dbHelper.edit(stringBuilder.toString());

                                        preferences = getSharedPreferences("chatNo", 0);
                                        editor = preferences.edit();
                                        editor.putInt(object.getString("roomId"), 0);
                                        editor.commit();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        alertDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                return;
                            }
                        });
                        alertDlg.setMessage("로그인 시 다른 핸드폰은 로그아웃 됩니다. 로그인 하시겠습니까?");
                        alertDlg.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (chk.equals("success")) {
                    preferences = getSharedPreferences("user", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("id", id);
                    editor.putString("Email", userEmail);
                    editor.putString("name", name);
                    editor.putString("stateMessage", stateMessage);
                    editor.putString("phoneNumber", phoneNumber);
                    editor.putString("profilePicture", profilePicture);
                    editor.putString("backgroundPhoto", backgroundPhoto);
                    editor.putString("token", token);
                    editor.commit();

                    dbHelper.edit("CREATE TABLE friend (friendNo INTEGER PRIMARY KEY AUTOINCREMENT, friendId TEXT, bookmark TEXT, friendName TEXT, userName TEXT, stateMessage TEXT, phoneNumber TEXT, profilePicture TEXT, backgroundPhoto TEXT);");
                    dbHelper.edit("CREATE TABLE room (roomId INTEGER PRIMARY KEY, title TEXT, subTitle TEXT, roomGubun TEXT);");
                    dbHelper.edit("CREATE TABLE roommate (roommateId INTEGER PRIMARY KEY AUTOINCREMENT, roomId INTEGER, userId TEXT, userName TEXT, stateMessage TEXT, profilePicture TEXT, backgroundPhoto TEXT, chatNo INTEGER);");
                    dbHelper.edit("CREATE TABLE chat (chatId INTEGER PRIMARY KEY AUTOINCREMENT, chatNo INTEGER, roomId INTEGER, userId TEXT, gubun TEXT, message TEXT, dtTm TEXT);");
                    dbHelper.edit("CREATE TABLE tempChat (chatId INTEGER PRIMARY KEY AUTOINCREMENT, roomId INTEGER, userId TEXT, gubun TEXT, message TEXT);");

                    try {
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

                        jsonArray = new JSONArray(jsonObject.getString("room"));
                        for (int i = 0; i < jsonArray.length(); i++) {
                            object = jsonArray.getJSONObject(i);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSERT INTO room VALUES(").append(object.getString("roomId")).append(", '");
                            stringBuilder.append(object.getString("title")).append("', '");
                            stringBuilder.append(object.getString("subTitle")).append("', '");
                            stringBuilder.append(object.getString("roomGubun")).append("')");
                            dbHelper.edit(stringBuilder.toString());
                        }

                        jsonArray = new JSONArray(jsonObject.getString("roommate"));
                        for (int i = 0; i < jsonArray.length(); i++) {
                            object = jsonArray.getJSONObject(i);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSERT INTO roommate VALUES(null, ");
                            stringBuilder.append(object.getString("roomId")).append(", '");
                            stringBuilder.append(object.getString("userId")).append("', '");
                            stringBuilder.append(object.getString("userName")).append("', '");
                            stringBuilder.append(object.getString("stateMessage")).append("', '");
                            stringBuilder.append(object.getString("profilePicture")).append("', '");
                            stringBuilder.append(object.getString("backgroundPhoto")).append("', '");
                            stringBuilder.append(object.getString("chatNo")).append("')");
                            dbHelper.edit(stringBuilder.toString());

                        }
                        jsonArray = new JSONArray(jsonObject.getString("chat"));
                        for (int i = 0; i < jsonArray.length(); i++) {
                            object = jsonArray.getJSONObject(i);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSERT INTO chat VALUES(null, ");
                            stringBuilder.append(object.getString("chatNo")).append(", ");
                            stringBuilder.append(object.getString("roomId")).append(", '");
                            stringBuilder.append(object.getString("userId")).append("', '");
                            stringBuilder.append(object.getString("gubun")).append("', '");
                            stringBuilder.append(object.getString("message")).append("', '");
                            stringBuilder.append(object.getString("dtTm")).append("')");
                            dbHelper.edit(stringBuilder.toString());

                            preferences = getSharedPreferences("chatNo", 0);
                            editor = preferences.edit();
                            editor.putInt(object.getString("roomId"), 0);
                            editor.commit();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    if (!chk.equals(""))
                        Toast.makeText(getApplicationContext(), chk, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String email = (String) params[0];
                    String password = (String) params[1];

                    String link = getString(R.string.ip) + "login.php";
                    String data = URLEncoder.encode("userEmail", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                    data += "&" + URLEncoder.encode("userPassword", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                    data += "&" + URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8");

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
        task.execute(email, password);
    }

    private void putToken(String bf, String af) {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String bftoken = (String) params[0];
                    String aftoken = (String) params[1];

                    String link = getString(R.string.ip) + "putToken.php";
                    String data = URLEncoder.encode("bftoken", "UTF-8") + "=" + URLEncoder.encode(bftoken, "UTF-8");
                    data += "&" + URLEncoder.encode("aftoken", "UTF-8") + "=" + URLEncoder.encode(aftoken, "UTF-8");

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
        task.execute(bf, af);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
