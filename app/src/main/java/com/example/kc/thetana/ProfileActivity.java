package com.example.kc.thetana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kc on 2017-02-25.
 */

public class ProfileActivity extends AppCompatActivity {
    Intent intent;
    TextView tv_statemessage;
    TextView tv_name;
    Button bt_chat;
    ImageButton ib_edit;
    String id, roomId;
    private SharedPreferences preferences;
    DBHelper dbHelper = new DBHelper(ProfileActivity.this, "thetana.db", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tv_statemessage = (TextView) findViewById(R.id.profile_tv_statemessage);
        tv_name = (TextView) findViewById(R.id.profile_tv_name);
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
                preferences = getSharedPreferences("friend", 0);
                try {
                    JSONObject jsonObject = new JSONObject(preferences.getString(id, ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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

        JSONObject jsonObject = dbHelper.getFriend(getIntent().getStringExtra("id"));
        try {
            tv_statemessage.setText(jsonObject.getString("stateMessage"));
            tv_name.setText(jsonObject.getString("friendName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
