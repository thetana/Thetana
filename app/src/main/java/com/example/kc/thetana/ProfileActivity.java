package com.example.kc.thetana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kc on 2017-02-25.
 */

public class ProfileActivity extends AppCompatActivity {
    Intent intent;
    TextView tv_statemessage;
    TextView tv_name;
    ImageView iv_profile, iv_background;
    Button bt_chat;
    ImageButton ib_edit;
    String id, roomId;
    private SharedPreferences preferences;
    DBHelper dbHelper = new DBHelper(ProfileActivity.this, "thetana.db", null, 1);
    String name = "", stateMessage = "", profilePicture = "", backgroundPhoto = "";
    AQuery aq = new AQuery(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tv_statemessage = (TextView) findViewById(R.id.profile_tv_statemessage);
        tv_name = (TextView) findViewById(R.id.profile_tv_name);
        iv_profile = (ImageView) findViewById(R.id.profile_iv_profile);
        iv_background = (ImageView) findViewById(R.id.profile_iv_background);
        bt_chat = (Button) findViewById(R.id.profile_bt_chat);
        ib_edit = (ImageButton) findViewById(R.id.profile_ib_edit);

        intent = this.getIntent();
        if (intent.getStringExtra("gubun").equals("me")) {
            bt_chat.setVisibility(View.GONE);
            ib_edit.setVisibility(View.GONE);
        }
        id = intent.getStringExtra("id");
        roomId = intent.getStringExtra("roomId");

        bt_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomGubun", "PtoP");
                intent.putExtra("isJoin", "N");

                startActivity(intent);
            }
        });
        ib_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ModifyFriendActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (intent.getStringExtra("gubun").equals("me")) {
            preferences = getSharedPreferences("user", 0);
            name = preferences.getString("name", "");
            stateMessage = preferences.getString("stateMessage", "");
            profilePicture = preferences.getString("profilePicture", "");
            backgroundPhoto = preferences.getString("backgroundPhoto", "");
        } else {
            JSONObject jsonObject = dbHelper.getFriend(getIntent().getStringExtra("id"));
            try {
                name = jsonObject.getString("friendName");
                stateMessage = jsonObject.getString("stateMessage");
                profilePicture = jsonObject.getString("profilePicture");
                backgroundPhoto = jsonObject.getString("backgroundPhoto");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        tv_name.setText(name);
        tv_statemessage.setText(stateMessage);
        if (!profilePicture.equals("")) aq.id(iv_profile).image(profilePicture);
        if (!backgroundPhoto.equals("")) aq.id(iv_background).image(backgroundPhoto);
    }
}
