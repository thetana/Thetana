package com.example.kc.thetana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class InviteActivity extends AppCompatActivity {
    Button bt_invite;
    ListView lv_friend;
    InviteAdapter adapter;
    SharedPreferences preferences;
    static DBHelper dbHelper;
    String roomId = "", roomGubun = "";
    private ArrayList<InviteItem> inviteItems = new ArrayList<InviteItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        dbHelper = new DBHelper(InviteActivity.this, "thetana.db", null, 1);
        roomId = getIntent().getStringExtra("roomId");
        roomGubun = getIntent().getStringExtra("roomGubun");
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
                for(int i = 0; i < adapter.getCount(); i++){
                    InviteItem inviteItem = (InviteItem) adapter.getItem(i);
                    if(inviteItem.checked) ids = ids + "," + inviteItem.id;
                }
                if(ids.equals("")) return;
                else ids = ids.substring(1);

                if(roomGubun.equals("PtoP")) roomId = "0";
                Intent intent = new Intent(InviteActivity.this, ChatActivity.class);
                intent.putExtra("id", ids);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomGubun", "Multi");
                intent.putExtra("isJoin", "Y");

                startActivity(intent);
            }
        });
    }
}
