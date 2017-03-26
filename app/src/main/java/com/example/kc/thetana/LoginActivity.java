package com.example.kc.thetana;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kc on 2017-02-14.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private EditText et_email;
    private EditText et_password;
    private Button bt_login;
    private Button bt_join;
    SharedPreferences preferences;
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";
    String token;
    DBHelper dbHelper = new DBHelper(this, "thetana.db", null, 1);

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

        //SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        //signInButton.setSize(SignInButton.SIZE_STANDARD);
//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signIn();
//            }
//        });

        et_email = (EditText) findViewById(R.id.login_et_email);
        et_password = (EditText) findViewById(R.id.login_et_password);
        bt_login = (Button) findViewById(R.id.login_bt_login);
        bt_join = (Button) findViewById(R.id.login_bt_join);

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
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
//        if (opr.isDone()) {
//            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
//            // and the GoogleSignInResult will be available instantly.
//            Log.d(TAG, "Got cached sign-in");
//            GoogleSignInResult result = opr.get();
//            handleSignInResult(result);
//        } else {
//            // If the user has not previously signed in on this device or the sign-in has expired,
//            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
//            // single sign-on will occur in this branch.
//            //showProgressDialog();
//            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
//                @Override
//                public void onResult(GoogleSignInResult googleSignInResult) {
//                    //hideProgressDialog();
//                    handleSignInResult(googleSignInResult);
//                }
//            });
//        }
//    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        Log.d(TAG, result.getStatus().toString());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
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
            GlobalClass globalClass;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(LoginActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String myJSON) {
                loading.dismiss();
                JSONObject jsonObject = null;
                JSONObject me = null;
                JSONObject chat = null;
                JSONArray chats = null;
                String chk = "";
                String id = "";
                String name = "";
                String stateMessage = "";

                try {
                    jsonObject = new JSONObject(myJSON);
                    me = jsonObject.getJSONArray("me").getJSONObject(0);

                    chk = me.getString("chk");
                    id = me.getString("id");
                    name = me.getString("name");
                    stateMessage = me.getString("stateMessage");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (chk.equals("success")) {
                    preferences = getSharedPreferences("user", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("id", id);
                    editor.putString("name", name);
                    editor.putString("stateMessage", stateMessage);
                    editor.putString("token", token);
                    editor.commit();

                    globalClass = new GlobalClass(context);
                    globalClass.updateFriends();

                    try {
                        chats = jsonObject.getJSONArray("chat");
                        for (int i = 0; i < chats.length(); i++) {
                            chat = chats.getJSONObject(i);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSERT INTO chat");
                            stringBuilder.append(" VALUES(").append(chat.getString("chatId")).append(", ");
                            stringBuilder.append(chat.getString("chatNo")).append(", ");
                            stringBuilder.append(chat.getString("roomId")).append(", '");
                            stringBuilder.append(chat.getString("userId")).append("', '");
                            stringBuilder.append(chat.getString("gubun")).append("', '");
                            stringBuilder.append(chat.getString("message")).append("', ");
                            stringBuilder.append(chat.getString("readed")).append(", '");
                            stringBuilder.append(chat.getString("insertDt")).append("', '");
                            stringBuilder.append(chat.getString("updateDt")).append("')");
                            dbHelper.edit(stringBuilder.toString());

                            preferences = getSharedPreferences("chatNo", 0);
                            editor = preferences.edit();
                            editor.putInt(chat.getString("roomId"), chat.getInt("chatNo"));
                            editor.commit();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    preferences = getSharedPreferences("update", 0);
                    editor = preferences.edit();
                    editor.putString("chat", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date()));
                    editor.commit();

                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
