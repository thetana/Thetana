package com.example.kc.thetana;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
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
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kc on 2017-02-18.
 */

public class MenuActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    static Context context;
    Menu myMenu;
    static DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        dbHelper = new DBHelper(MenuActivity.this, "thetana.db", null, 1);
        FirebaseMessaging.getInstance().subscribeToTopic("news");

        context = MenuActivity.this;
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

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static Context myContext;
        ExpandableListView elv;
        ListView lv_room;
        FriendExpandableAdapter friendExpandableAdapter = new FriendExpandableAdapter();
        RoomAdapter roomAdapter = new RoomAdapter();
        Button bt_logout;
        SharedPreferences preferences;
        GlobalClass globalClass;

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
                inflater.inflate(R.menu.menu_chat, menu);
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
            }
            if (id == R.id.frinend_action_set) {
                globalClass = new GlobalClass(context);
                globalClass.updateFriends();
                friendExpandableAdapter.setGroup(globalClass.getFriends());
                return true;
            }
            if (id == R.id.room_action_addRoom) {
                Intent intent = new Intent(myContext, InviteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onStart() {
            super.onStart();

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                globalClass = new GlobalClass(context);
                globalClass.updateFriends();
                friendExpandableAdapter.setGroup(globalClass.getFriends());
                for (int i = 0; i < friendExpandableAdapter.getGroupCount(); i++) {
                    elv.expandGroup(i);
                }
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                globalClass = new GlobalClass(context);
                globalClass.updateRooms();
                roomAdapter.setItem(globalClass.getRooms());
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {

            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_friend, container, false);

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                globalClass = new GlobalClass(context);
                globalClass.updateFriends();
                friendExpandableAdapter.setGroup(globalClass.getFriends());

                rootView = inflater.inflate(R.layout.fragment_friend, container, false);
                elv = (ExpandableListView) rootView.findViewById(R.id.elv);
                elv.setAdapter(friendExpandableAdapter);
                //friendExpandableAdapter.setGroup(globalClass.getFriends());

//                for (int i = 0; i < friendExpandableAdapter.getGroupCount(); i++) {
//                    elv.expandGroup(i);
//                }

                elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        FriendChild friendChild = (FriendChild) friendExpandableAdapter.getChild(groupPosition, childPosition);
                        Intent intent = new Intent(container.getContext(), ProfileActivity.class);

                        intent.putExtra("gubun", "");
                        if (groupPosition == 0) intent.putExtra("gubun", "me");

                        intent.putExtra("id", friendChild.id);
                        intent.putExtra("name", friendChild.name);
                        intent.putExtra("state", friendChild.state);
                        intent.putExtra("roomId", friendChild.roomId);

                        startActivity(intent);
                        return false;
                    }
                });

            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                globalClass = new GlobalClass(context);
                globalClass.updateRooms();
                roomAdapter.setItem(globalClass.getRooms());
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

                        startActivity(intent);
                    }
                });
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
                rootView = inflater.inflate(R.layout.fragment_setting, container, false);
                bt_logout = (Button) rootView.findViewById(R.id.setting_bt_logout);
                bt_logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logout(container.getContext().getSharedPreferences("user", 0).getString("id", ""));

                        dbHelper.edit("DELETE FROM chat;");

                        preferences = container.getContext().getSharedPreferences("user", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();

                        preferences = container.getContext().getSharedPreferences("friend", 0);
                        editor = preferences.edit();
                        editor.clear();
                        editor.commit();

                        preferences = container.getContext().getSharedPreferences("room", 0);
                        editor = preferences.edit();
                        editor.clear();
                        editor.commit();

                        Intent intent = new Intent(container.getContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                        startActivity(intent);
                    }
                });
            }
            return rootView;
        }





        private void logout(String id) {
            class GetDataJSON extends AsyncTask<String, Void, String> {
                ProgressDialog loading;
                GlobalClass globalClass;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(context, "Please Wait", null, true, true);
                }

                @Override
                protected void onPostExecute(String myJSON) {
                    loading.dismiss();
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