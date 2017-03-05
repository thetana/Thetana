package com.example.kc.thetana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
    String id, roomId;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tv_statemessage = (TextView) findViewById(R.id.profile_tv_statemessage);
        tv_name = (TextView) findViewById(R.id.profile_tv_name);
        bt_chat = (Button) findViewById(R.id.profile_bt_chat);

        intent = this.getIntent();
        if(intent.getStringExtra("gubun").equals("me")) bt_chat.setVisibility(View.GONE);
        id = intent.getStringExtra("id");
        roomId = intent.getStringExtra("roomId");

        tv_statemessage.setText(intent.getStringExtra("state"));
        tv_name.setText(intent.getStringExtra("name"));
        bt_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = getSharedPreferences("friend", 0);
                try {
                    JSONObject jsonObject =  new JSONObject(preferences.getString(id, ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("roomId", roomId);

                startActivity(intent);
            }
        });

    }
}
