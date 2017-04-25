package com.example.kc.thetana;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

/**
 * Created by kc on 2017-02-25.
 */

public class ChatActivity extends ActionBarActivity {
    String imgDecodableString;
    String roomId = "", roomGubun = "";
    private DrawerLayout dl_drawer;
    ListView lv_roommate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        dl_drawer = (DrawerLayout) findViewById(R.id.chat_dl_drawer);
        lv_roommate = (ListView) findViewById(R.id.chat_lv_roommate);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        roomId = getIntent().getStringExtra("roomId");
        roomGubun = getIntent().getStringExtra("roomGubun");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.chat_action_drawer) {
            if (dl_drawer.isDrawerOpen(Gravity.RIGHT)) {
                dl_drawer.closeDrawer(Gravity.RIGHT);
            } else {
                dl_drawer.openDrawer(Gravity.RIGHT);
            }
            if (item != null && item.getItemId() == android.R.id.home) {
            }
//            Intent intent = new Intent(ChatActivity.this, InviteActivity.class);
//            intent.putExtra("roomId", roomId);
//            intent.putExtra("roomGubun", roomGubun);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//            startActivity(intent);

            return true;
        } else if (id == R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openGallery() {
        Intent galleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryintent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK
                && null != data) {
//            Uri selectedImage = data.getData();
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            // Move to first row
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            imgDecodableString = cursor.getString(columnIndex);
//            cursor.close();
//            //Log.d("onActivityResult",imgDecodableString);
//            ChatFragment fragment = (ChatFragment) getFragmentManager().findFragmentById(R.id.chat);
//            fragment.sendImage(imgDecodableString);
        }
    }
}