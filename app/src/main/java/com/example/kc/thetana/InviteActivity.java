package com.example.kc.thetana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class InviteActivity extends AppCompatActivity {
    Button bt_serch;
    ListView lv_friend;
    InviteAdapter adapter;
    SharedPreferences preferences;

    private ArrayList<InviteItem> inviteItems = new ArrayList<InviteItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        bt_serch = (Button) findViewById(R.id.invite_bt_invite);
        lv_friend = (ListView) findViewById(R.id.invite_lv_friend);
        adapter = new InviteAdapter();
        lv_friend.setAdapter(adapter);

        preferences = getSharedPreferences("friend", 0);
        Iterator<String> iterator = preferences.getAll().keySet().iterator();
        while (iterator.hasNext()) {
            try {
                InviteItem inviteItem = new InviteItem();
                JSONObject jsonObject = new JSONObject(preferences.getString(iterator.next(), ""));
                inviteItem.id = jsonObject.getString("friendId");
                inviteItem.name = jsonObject.getString("userName");
                inviteItems.add(inviteItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.setItem(inviteItems);
        lv_friend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InviteItem inviteItem = (InviteItem) adapter.getItem(position);
                if (inviteItem.checked) inviteItem.checked = false;
                else inviteItem.checked = true;
                adapter.notifyDataSetChanged();
            }
        });

        bt_serch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ids = "";
                for(int i = 0; i < adapter.getCount(); i++){
                    InviteItem inviteItem = (InviteItem) adapter.getItem(i);
                    if(inviteItem.checked) ids = ids + "," + inviteItem.id;
                }
                if(ids.equals("")) return;
                else ids = ids.substring(1);

                Intent intent = new Intent(InviteActivity.this, ChatActivity.class);
                intent.putExtra("id", ids);
                intent.putExtra("roomId", "");
                intent.putExtra("roomGubun", "Multi");

                startActivity(intent);
            }
        });
    }
}
