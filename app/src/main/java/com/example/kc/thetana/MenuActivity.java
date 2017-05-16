package com.example.kc.thetana;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by kc on 2017-02-18.
 */

public class MenuActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    static Context context;
    Menu myMenu;
    static DBHelper dbHelper;
    static String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

//        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
//        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);


        dbHelper = new DBHelper(MenuActivity.this, "thetana.db", null, 1);
        FirebaseMessaging.getInstance().subscribeToTopic("news");

        context = MenuActivity.this;
        myId = getSharedPreferences("user", 0).getString("id", "");
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), MenuActivity.this);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public static class PlaceholderFragment extends Fragment {

        ChatThread chatThread;
        RoomHandler handler;
        String ServerIP = "35.163.3.139";
        //        Socket socket;
        DataOutputStream out;

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static Context myContext;
        ExpandableListView elv;
        EditText et_roomSerch, et_friendSerch;
        ListView lv_room;
        FriendExpandableAdapter friendExpandableAdapter = new FriendExpandableAdapter();
        RoomAdapter roomAdapter = new RoomAdapter();
        Button bt_setting, bt_change, bt_logout, bt_quit;
        SharedPreferences preferences;
        private ArrayList<FriendGroup> friendGroup = new ArrayList<FriendGroup>();
        private ArrayList<RoomItem> roomItems = new ArrayList<RoomItem>();

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber, Context context) {
            myContext = context;
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }


        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                inflater.inflate(R.menu.menu_main, menu);
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                inflater.inflate(R.menu.menu_room, menu);
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
                //inflater.inflate(R.menu.menu_main, menu);
            }

            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.frinend_action_addFriend) {
                Intent intent = new Intent(myContext, FriendActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.frinend_action_set) {
                return true;
            } else if (id == R.id.room_action_addRoom) {
                Intent intent = new Intent(myContext, InviteActivity.class);
                intent.putExtra("roomId", "0");
                intent.putExtra("roomGubun", "Multi");
                intent.putExtra("friend", "");
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        class RoomHandler extends Handler {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.getData().getString("order").equals("newRoom")) getRoom();
            }
        }

        @Override
        public void onStart() {
            super.onStart();
//            handler = new RoomHandler();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        socket = new Socket(ServerIP, 9999);
//                        out = new DataOutputStream(socket.getOutputStream());
//                        out.writeUTF(myId);
//                        out.writeUTF("0");
//                        chatThread = new ChatThread(handler, socket);
//                        chatThread.start();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                friendGroup = new ArrayList<FriendGroup>();
                friendGroup.add(new FriendGroup("프로필"));
                friendGroup.add(new FriendGroup("즐겨찾기"));
                friendGroup.add(new FriendGroup("친구"));

                preferences = context.getSharedPreferences("user", 0);
                friendGroup.get(0).friendChildren.add(new FriendChild());
                friendGroup.get(0).friendChildren.get(0).id = preferences.getString("id", "");
                friendGroup.get(0).friendChildren.get(0).name = preferences.getString("name", "");
                friendGroup.get(0).friendChildren.get(0).state = preferences.getString("stateMessage", "");
                friendGroup.get(0).friendChildren.get(0).profile = preferences.getString("profilePicture", "");
                friendGroup.get(0).friendChildren.get(0).background = preferences.getString("backgroundPhoto", "");

                try {
                    JSONObject jsonObject = dbHelper.getFriends("");
                    JSONArray jsonArray = jsonObject.getJSONArray("friend");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String friendId = object.getString("friendId");
                        String bookmark = object.getString("bookmark");
                        String friendName = object.getString("friendName");
                        String stateMessage = object.getString("stateMessage");
                        String profilePicture = object.getString("profilePicture");
                        String backgroundPhoto = object.getString("backgroundPhoto");
                        String roomId = object.getString("roomId");

                        FriendChild friendChild = new FriendChild();
                        friendChild.id = friendId;
                        friendChild.name = friendName;
                        friendChild.state = stateMessage;
                        friendChild.roomId = roomId;
                        friendChild.profile = profilePicture;
                        friendChild.background = backgroundPhoto;

                        friendGroup.get(2).friendChildren.add(friendChild);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                friendExpandableAdapter.setGroup(friendGroup);
                for (int i = 0; i < friendExpandableAdapter.getGroupCount(); i++) {
                    elv.expandGroup(i);
                }
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (null != activeNetwork) getRoom();
                else {
                    JSONObject jsonObject = null;
                    JSONObject object = null;
                    JSONArray jsonArray = null;
                    jsonObject = dbHelper.getRoom("");
                    try {
                        jsonArray = jsonObject.getJSONArray("room");
//                        roomItems = new ArrayList<RoomItem>();
                        roomAdapter.clearItem();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            object = jsonArray.getJSONObject(i);
                            String roomId = object.getString("roomId");
                            String subTitle = object.getString("subTitle");
                            String roomGubun = object.getString("roomGubun");
                            String roomName = object.getString("roomName");
                            String profilePicture = object.getString("profilePicture");

                            RoomItem roomItem = new RoomItem();
                            roomItem.id = roomId;
                            roomItem.gubun = roomGubun;
                            roomItem.name = roomName;
                            roomItem.pictrue = profilePicture;

                            roomAdapter.addItem(roomItem);
//                            roomItems.add(roomItem);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
            }
        }

        //        @Override
//        public void onStop() {
//            super.onStop();
//            try {
//                if (socket != null) socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            context = activity;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_friend, container, false);

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {

                rootView = inflater.inflate(R.layout.fragment_friend, container, false);
                et_friendSerch = (EditText) rootView.findViewById(R.id.myFriend_et_serch);
                elv = (ExpandableListView) rootView.findViewById(R.id.elv);

                elv.setAdapter(friendExpandableAdapter);
                //friendExpandableAdapter.setGroup(globalClass.getFriends());

                elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        FriendChild friendChild = (FriendChild) friendExpandableAdapter.getChild(groupPosition, childPosition);
                        Intent intent = new Intent(container.getContext(), ProfileActivity.class);

                        intent.putExtra("gubun", "");
                        if (groupPosition == 0) intent.putExtra("gubun", "me");

                        intent.putExtra("id", friendChild.id);
                        intent.putExtra("roomId", friendChild.roomId);

                        startActivity(intent);
                        return false;
                    }
                });

            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                rootView = inflater.inflate(R.layout.fragment_room, container, false);

                lv_room = (ListView) rootView.findViewById(R.id.room_lv_room);
                lv_room.setAdapter(roomAdapter);
                lv_room.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RoomItem roomItem = (RoomItem) roomAdapter.getItem(position);
                        Intent intent = new Intent(container.getContext(), ChatActivity.class);

                        intent.putExtra("roomId", roomItem.id);
                        intent.putExtra("roomGubun", roomItem.gubun);
                        intent.putExtra("isJoin", "N");

                        startActivity(intent);
                    }
                });
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
                rootView = inflater.inflate(R.layout.fragment_setting, container, false);
                bt_setting = (Button) rootView.findViewById(R.id.setting_bt_setting);
                bt_change = (Button) rootView.findViewById(R.id.setting_bt_change);
                bt_logout = (Button) rootView.findViewById(R.id.setting_bt_logout);
                bt_quit = (Button) rootView.findViewById(R.id.setting_bt_quit);
                bt_setting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(container.getContext(), ModifyMeActivity.class);
                        startActivity(intent);
                    }
                });
                bt_change.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                bt_logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logout(container.getContext().getSharedPreferences("user", 0).getString("id", ""));

                        dbHelper.edit("DROP TABLE friend;");
                        dbHelper.edit("DROP TABLE room;");
                        dbHelper.edit("DROP TABLE roommate;");
                        dbHelper.edit("DROP TABLE chat;");
                        dbHelper.edit("DROP TABLE tempChat;");

                        preferences = container.getContext().getSharedPreferences("user", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();

                        preferences = container.getContext().getSharedPreferences("now", 0);
                        editor = preferences.edit();
                        editor.clear();
                        editor.commit();

                        preferences = container.getContext().getSharedPreferences("friend", 0);
                        editor = preferences.edit();
                        editor.clear();
                        editor.commit();

                        preferences = container.getContext().getSharedPreferences("chatNo", 0);
                        editor = preferences.edit();
                        editor.clear();
                        editor.commit();

                        preferences = container.getContext().getSharedPreferences("update", 0);
                        editor = preferences.edit();
                        editor.clear();
                        editor.commit();

                        Intent intent = new Intent(container.getContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                bt_quit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            return rootView;
        }

        private void getRoom() {

            class getData extends AsyncTask<String, Void, String> {
                ProgressDialog loading;
                DBHelper dbHelper = new DBHelper(context, "thetana.db", null, 1);

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(context, "Please Wait", null, true, true);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    loading.dismiss();

                    JSONObject jsonObject = null;
                    JSONObject object = null;
                    JSONArray jsonArray = null;

                    try {
                        jsonObject = new JSONObject(s);

                        jsonArray = new JSONArray(jsonObject.getString("room"));
                        dbHelper.edit("DELETE FROM room;");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            object = jsonArray.getJSONObject(i);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSERT INTO room VALUES(").append(object.getString("roomId")).append(", '");
                            stringBuilder.append(object.getString("title")).append("', '");
                            stringBuilder.append(object.getString("subTitle")).append("', '");
                            stringBuilder.append(object.getString("roomGubun")).append("')");
                            dbHelper.edit(stringBuilder.toString());
                        }

                        jsonArray = new JSONArray(jsonObject.getString("roommate"));
                        dbHelper.edit("DELETE FROM roommate;");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            object = jsonArray.getJSONObject(i);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSERT INTO roommate VALUES(null, ");
                            stringBuilder.append(object.getString("roomId")).append(", '");
                            stringBuilder.append(object.getString("userId")).append("', '");
                            stringBuilder.append(object.getString("userName")).append("', '");
                            stringBuilder.append(object.getString("stateMessage")).append("', '");
                            stringBuilder.append(object.getString("profilePicture")).append("', '");
                            stringBuilder.append(object.getString("backgroundPhoto")).append("', '");
                            stringBuilder.append(object.getString("chatNo")).append("')");
                            dbHelper.edit(stringBuilder.toString());
                        }

                        jsonObject = dbHelper.getRoom("");
                        jsonArray = jsonObject.getJSONArray("room");
//                        roomItems = new ArrayList<RoomItem>();
                        roomAdapter.clearItem();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            object = jsonArray.getJSONObject(i);
                            String roomId = object.getString("roomId");
                            String subTitle = object.getString("subTitle");
                            String roomGubun = object.getString("roomGubun");
                            String roomName = object.getString("roomName");
                            String profilePicture = object.getString("profilePicture");

                            RoomItem roomItem = new RoomItem();
                            roomItem.id = roomId;
                            roomItem.gubun = roomGubun;
                            roomItem.name = roomName;
                            roomItem.pictrue = profilePicture;

                            roomAdapter.addItem(roomItem);
//                            roomItems.add(roomItem);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    roomAdapter.setItem(roomItems);
                }

                @Override
                protected String doInBackground(String... params) {
                    try {
                        String link = context.getString(R.string.ip) + "getRoom.php";
                        String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(myId, "UTF-8");

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
                        return sb.toString();
                    } catch (Exception e) {
                        return new String("Exception: " + e.getMessage());
                    }
                }
            }
            getData task = new getData();
            task.execute();
        }

        private void logout(String id) {
            class GetDataJSON extends AsyncTask<String, Void, String> {
                ProgressDialog loading;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    //loading = ProgressDialog.show(context, "Please Wait", null, true, true);
                }

                @Override
                protected void onPostExecute(String myJSON) {
                    //loading.dismiss();
                }

                @Override
                protected String doInBackground(String... params) {
                    try {
                        String userId = (String) params[0];

                        String link = getString(R.string.ip) + "logout.php";
                        String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");

                        URL url = new URL(link);
                        URLConnection conn = url.openConnection();

                        conn.setDoOutput(true);
                        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                        wr.write(data);
                        wr.flush();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        return "";
                    } catch (Exception e) {
                        return new String("Exception: " + e.getMessage());
                    }
                }
            }
            GetDataJSON task = new GetDataJSON();
            task.execute(id);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Context context;
        int position = 0;

        public SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            this.position = position;

            return PlaceholderFragment.newInstance(position + 1, context);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "친구";
                case 1:
                    return "채팅";
                case 2:
                    return "더보기";
            }
            return null;
        }
    }
}