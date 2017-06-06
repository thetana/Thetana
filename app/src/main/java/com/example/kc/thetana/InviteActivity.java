package com.example.kc.thetana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InviteActivity extends AppCompatActivity {
    Button bt_invite;
    ListView lv_friend;
    InviteAdapter adapter;
    SharedPreferences preferences;
    static DBHelper dbHelper;
    String roomId = "", roomGubun = "";
    private ArrayList<InviteItem> inviteItems = new ArrayList<InviteItem>();
    ImageButton ib_get;
    TextView tb_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        roomId = getIntent().getStringExtra("roomId");
        roomGubun = getIntent().getStringExtra("roomGubun");

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
        if(roomId.equals("0") || roomGubun.equals("PtoP")) tb_title.setText("채팅방만들기");
        else tb_title.setText("친구초대");
        ib_get = (ImageButton) actionbar.findViewById(R.id.ib_get);
        ib_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ids = "";
                if (roomGubun.equals("PtoP")) {
                    roomId = "0";
                    ids = ids + "," + getIntent().getStringExtra("friend");
                }
                for (int i = 0; i < adapter.getCount(); i++) {
                    InviteItem inviteItem = (InviteItem) adapter.getItem(i);
                    if (inviteItem.checked) {
                        if (dbHelper.isNewRoommate(roomId, inviteItem.id))
                            ids = ids + "," + inviteItem.id;
                    }
                }
                if (ids.equals("")) return;
                else ids = ids.substring(1);

                Intent intent = new Intent(InviteActivity.this, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", ids);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomGubun", "Multi");
                if (roomId.equals("0")) intent.putExtra("isJoin", "N");
                else intent.putExtra("isJoin", "Y");

                startActivity(intent);
            }
        });
        dbHelper = new DBHelper(InviteActivity.this, "thetana.db", null, 1);
        bt_invite = (Button) findViewById(R.id.invite_bt_invite);
        lv_friend = (ListView) findViewById(R.id.invite_lv_friend);
        adapter = new InviteAdapter();
        lv_friend.setAdapter(adapter);

        try {
            JSONObject jsonObject = dbHelper.getInvite(roomId);
            JSONArray jsonArray = jsonObject.getJSONArray("friend");
            for (int i = 0; i < jsonArray.length(); i++) {
                InviteItem inviteItem = new InviteItem();

                JSONObject object = jsonArray.getJSONObject(i);
                inviteItem.id = object.getString("friendId");
                inviteItem.name = object.getString("friendName");
                inviteItem.profile = object.getString("profilePicture");

                inviteItems.add(inviteItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.setItem(inviteItems);

//        preferences = getSharedPreferences("friend", 0);
//        Iterator<String> iterator = preferences.getAll().keySet().iterator();
//        while (iterator.hasNext()) {
//            try {
//                InviteItem inviteItem = new InviteItem();
//                JSONObject jsonObject = new JSONObject(preferences.getString(iterator.next(), ""));
//                inviteItem.id = jsonObject.getString("friendId");
//                inviteItem.name = jsonObject.getString("userName");
//                inviteItems.add(inviteItem);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        adapter.setItem(inviteItems);

        lv_friend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InviteItem inviteItem = (InviteItem) adapter.getItem(position);
                if (inviteItem.checked) inviteItem.checked = false;
                else inviteItem.checked = true;
                adapter.notifyDataSetChanged();
            }
        });

        bt_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ids = "";
                if (roomGubun.equals("PtoP")) {
                    roomId = "0";
                    ids = ids + "," + getIntent().getStringExtra("friend");
                }
                for (int i = 0; i < adapter.getCount(); i++) {
                    InviteItem inviteItem = (InviteItem) adapter.getItem(i);
                    if (inviteItem.checked) {
                        if (dbHelper.isNewRoommate(roomId, inviteItem.id))
                            ids = ids + "," + inviteItem.id;
                    }
                }
                if (ids.equals("")) return;
                else ids = ids.substring(1);

                Intent intent = new Intent(InviteActivity.this, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", ids);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomGubun", "Multi");
                if (roomId.equals("0")) intent.putExtra("isJoin", "N");
                else intent.putExtra("isJoin", "Y");

                startActivity(intent);
            }
        });
    }
}
