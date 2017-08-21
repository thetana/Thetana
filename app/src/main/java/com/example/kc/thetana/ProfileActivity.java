package com.example.kc.thetana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    TextView tv_name, tb_title;
    ImageView iv_profile, iv_background;
    ImageButton ib_edit, ib_get;
    String id, roomId;
    private SharedPreferences preferences;
    DBHelper dbHelper = new DBHelper(ProfileActivity.this, "thetana.db", null, 1);
    String name = "", stateMessage = "", profilePicture = "", backgroundPhoto = "";
    AQuery aq = new AQuery(this);
    LinearLayout ll_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        aq.id(ib_get).image(R.drawable.asset73);
        ib_get.setOnClickListener(new View.OnClickListener() {
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
        tv_statemessage = (TextView) findViewById(R.id.profile_tv_statemessage);
        tv_name = (TextView) findViewById(R.id.profile_tv_name);
        iv_profile = (ImageView) findViewById(R.id.profile_iv_profile);
        iv_background = (ImageView) findViewById(R.id.profile_iv_background);
        ib_edit = (ImageButton) findViewById(R.id.profile_ib_edit);
        intent = this.getIntent();
        if (intent.getStringExtra("gubun").equals("me")) {
            ib_get.setVisibility(View.GONE);
            ib_edit.setVisibility(View.GONE);
            ll_name = (LinearLayout) findViewById(R.id.ll_name);
            ll_name.setPadding(0,0,0,0);
        }
        id = intent.getStringExtra("id");
        roomId = intent.getStringExtra("roomId");

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
        tb_title.setText(name);
        tv_statemessage.setText(stateMessage);
//        if (!profilePicture.equals("")) aq.id(iv_profile).image(profilePicture);
        if (!backgroundPhoto.equals("")) aq.id(iv_background).image(backgroundPhoto);

        ImageView imageView = new ImageView(this);
        if (!profilePicture.equals("")) aq.id(imageView).image(profilePicture);
        ImageHandler handler = new ImageHandler(imageView, iv_profile);
        ImageThread thread = new ImageThread(handler, imageView);
        thread.start();
    }
}
