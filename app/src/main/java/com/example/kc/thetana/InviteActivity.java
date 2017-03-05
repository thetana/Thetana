package com.example.kc.thetana;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

public class InviteActivity extends AppCompatActivity {
    Button bt_serch;
    ListView lv_friend;
    InviteAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        bt_serch = (Button) findViewById(R.id.invite_bt_invite);
        lv_friend = (ListView) findViewById(R.id.invite_lv_friend);
        adapter = new InviteAdapter();
        lv_friend.setAdapter(adapter);

        lv_friend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InviteItem inviteItem = (InviteItem)adapter.getItem(position);
                if(inviteItem.checked) inviteItem.checked = false;
                else inviteItem.checked = true;
                adapter.notifyDataSetChanged();
            }
        });

//        bt_serch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                preferences = getSharedPreferences("friend", 0);
//                try {
//                    JSONObject jsonObject =  new JSONObject(preferences.getString(id, ""));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                Intent intent = new Intent(InviteActivity.this, ChatActivity.class);
//                intent.putExtra("id", id);
//                intent.putExtra("roomId", roomId);
//
//                startActivity(intent);
//
//            }
//        });



    }
}
