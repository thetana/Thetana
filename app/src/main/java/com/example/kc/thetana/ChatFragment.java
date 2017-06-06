package com.example.kc.thetana;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by kc on 2017-02-25.
 */

public class ChatFragment extends Fragment {

    static Context context;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    AQuery aq;
    private EditText mInputMessageView;
    ListView listView;
    LinearLayout ll_on;
    private List<Message> mMessages = new ArrayList<Message>();
    public ArrayList<Roommate> roommateList = new ArrayList<Roommate>();
    public Hashtable<String, Roommate> roommateMap = new Hashtable<String, Roommate>();
    private Hashtable<String, Integer> mHashtable = new Hashtable<String, Integer>();
    RoommateAdapter roommateAdapter = new RoommateAdapter();
    ChatAdapter adapter = new ChatAdapter();
    SendHandler sendHandler;
    ChatThread chatThread;
    ChatHandler handler;
    String ServerIP = "35.163.3.139";
    Socket socket;
    DataOutputStream out;
    String room = "", myId = "", roomGubun = "", myName = "", myProfile, isJoin = "N";
    String myChatNo = "0", lastChatNo = "0";
    DBHelper dbHelper;
    boolean isFirst = true, isBottom = true;
    public int visibility = View.GONE;
    Thread alive;
    IntentFilter filter;
    NetworkReceiver receiver;

    public ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;

        aq = new AQuery(context);
        dbHelper = new DBHelper(context, "thetana.db", null, 1);
        myId = context.getSharedPreferences("user", 0).getString("id", "");
        myName = context.getSharedPreferences("user", 0).getString("name", "");
        myProfile = context.getSharedPreferences("user", 0).getString("profilePicture", "");
        room = ((Activity) context).getIntent().getStringExtra("roomId");
        roomGubun = ((Activity) context).getIntent().getStringExtra("roomGubun");
        isJoin = ((Activity) context).getIntent().getStringExtra("isJoin");

        JSONObject jsonObject = dbHelper.getRoommate(room);
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("roommate");

            roommateList.add(0, new Roommate(myId));
            roommateList.get(0).userName = myName;
            roommateList.get(0).profilePicture = myProfile;
            roommateList.get(0).onOff = true;
            roommateList.get(0).iv = new ImageView(context);
            roommateMap.put(myId, roommateList.get(0));
            aq.id(roommateList.get(0).iv).image(R.drawable.asset55); // icic

            if (!roommateList.get(0).profilePicture.equals(""))
                aq.id(roommateList.get(0).iv).image(roommateList.get(0).profilePicture);
//            ImageHandler handler = new ImageHandler(roommateList.get(0).iv);
//            ImageThread thread = new ImageThread(handler, roommateList.get(0).iv);
//            thread.start();


            for (int i = 1; i <= jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i - 1);
                roommateList.add(i, new Roommate(object.getString("userId")));
                roommateList.get(i).userName = object.getString("userName");
                roommateList.get(i).profilePicture = object.getString("profilePicture");
                roommateList.get(i).chatNo = object.getInt("chatNo");


                roommateList.get(i).iv = new ImageView(context);
                roommateList.get(i).tv = new TextView(context);
                roommateList.get(i).tv.setVisibility(View.GONE);

                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                params.weight = 0;
                roommateList.get(i).iv.setLayoutParams(params);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 0;
                roommateList.get(i).tv.setLayoutParams(layoutParams);
                roommateList.get(i).tv.setText(object.getString("userName"));
                aq.id(roommateList.get(i).iv).image(R.drawable.asset55); // icic

                if (!roommateList.get(i).profilePicture.equals(""))
                    aq.id(roommateList.get(i).iv).image(roommateList.get(i).profilePicture);
//                ImageHandler imageHandler = new ImageHandler(roommateList.get(i).iv);
//                ImageThread imageThread = new ImageThread(imageHandler, roommateList.get(i).iv);
//                imageThread.start();

                roommateList.get(i).viewMap.put(roommateList.get(i).iv, roommateList.get(i).tv);
                roommateMap.put(object.getString("userId"), roommateList.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ll_on = (LinearLayout) view.findViewById(R.id.chat_ll_on);
        listView = (ListView) view.findViewById(R.id.chat_lv_list);
        listView.setAdapter(adapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount)
                    isBottom = true;
                else isBottom = false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = (Message) parent.getAdapter().getItem(position);
                if (message.gubun.equals("message")) {
                    if (message.visibility == View.GONE) {
                        if (message.chatNo.equals("")) return;
                        int mChatNo = Integer.parseInt(message.chatNo);
                        int count = 0;
                        for (int i = 0; i < roommateList.size(); i++) {
                            if (!roommateList.get(i).onOff && roommateList.get(i).chatNo < mChatNo) {
                                count++;
                            }
                        }
                        if (count == 0) message.number = "다 읽음.";
                        else message.number = String.valueOf(count) + "명 안읽음.";
                        message.visibility = View.VISIBLE;
                    } else if (message.visibility == View.VISIBLE) {
                        message.number = "";
                        message.visibility = View.GONE;
                    }
                } else if (message.gubun.equals("state")) {
                    if (message.visibility == View.GONE) message.visibility = View.VISIBLE;
                    else if (message.visibility == View.VISIBLE) message.visibility = View.GONE;
                }
                adapter.notifyDataSetChanged();
            }
        });

        ll_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibility == View.GONE) visibility = View.VISIBLE;
                else if (visibility == View.VISIBLE) visibility = View.GONE;
                for (int i = 1; i < ll_on.getChildCount(); i++) {
                    if (ll_on.getChildAt(i) instanceof TextView) {
                        ll_on.getChildAt(i).setVisibility(visibility);
                    }
                }
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.chat_bt_send);
        mInputMessageView = (EditText) view.findViewById(R.id.chat_et_message);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        handler = new ChatHandler();
        sendHandler = new SendHandler();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(ServerIP, 9999);
                        out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF(myId);
                        out.writeUTF(room);
                        chatThread = new ChatThread(handler, socket);
                        chatThread.start();
                        if (isJoin.equals("Y") && isFirst) {
                            JSONObject jsonObject = new JSONObject();
                            JSONArray array = new JSONArray();
                            String[] ids = ((Activity) context).getIntent().getStringExtra("id").split(",");
                            try {
                                for (int i = 0; i < ids.length; i++) {
                                    array.put(i, ids[i]);
                                }
                                jsonObject.put("order", "invite");
                                jsonObject.put("room", room);
                                jsonObject.put("friends", array);
                                out.writeUTF(jsonObject.toString());
                                isFirst = false;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
        JSONObject jsonObject = null;
        JSONObject object = null;
        JSONArray jsonArray = null;
        try {
            jsonObject = dbHelper.getChat(room);
            jsonArray = jsonObject.getJSONArray("chat");
            adapter.clearMessage();
            for (int i = 0; i < jsonArray.length(); i++) {
                object = jsonArray.getJSONObject(i);
                Message message = new Message();
                if (roommateMap.get(object.getString("userId")) != null)
                    message.name = roommateMap.get(object.getString("userId")).userName;
                message.chatNo = object.getString("chatNo");
                message.userId = object.getString("userId");
                message.gubun = object.getString("gubun");
                message.profile = object.getString("profilePicture");
                message.text = object.getString("message");
                message.dtTm = object.getString("dtTm");

                adapter.addMessage(message);
                lastChatNo = message.chatNo;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = context.getSharedPreferences("now", 0).edit();
        editor.putString("room", room);
        editor.commit();
        roommateAdapter = new RoommateAdapter();
        roommateAdapter.putItems(roommateList);
        ((ChatActivity) getActivity()).lv_roommate.setAdapter(roommateAdapter);

        if (null == activeNetwork) {
            for (int i = 0; i < roommateList.size(); i++) {
                ll_on.removeView(roommateList.get(i).iv);
                ll_on.removeView(roommateList.get(i).tv);
                if (ll_on.getChildCount() < 2) ll_on.setVisibility(View.GONE);

                int index = adapter.getIndex(String.valueOf(roommateList.get(i).chatNo));
                if (index + 1 < adapter.getCount() && ((Message) adapter.getItem(index + 1)).gubun.equals("state")) {
                    ((Message) adapter.getItem(index + 1)).iv.remove(roommateList.get(i).iv);
                    ((Message) adapter.getItem(index + 1)).iv.add(roommateList.get(i).iv);
                    ((Message) adapter.getItem(index + 1)).tv.put(roommateList.get(i).iv, roommateList.get(i).tv);
                    adapter.notifyDataSetChanged();
                } else {
                    Message message = new Message();
                    message.gubun = "state";
                    message.iv.add(roommateList.get(i).iv);
                    message.tv.put(roommateList.get(i).iv, roommateList.get(i).tv);
                    adapter.addMessage(index + 1, message);
                }
            }
            adapter.calculate(roommateList);
        }

        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver(ChatFragment.this);
        context.registerReceiver(receiver, filter);

        alive = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (socket != null) out.writeUTF("alive");
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
            }
        });
        alive.start();
    }

    class ChatHandler extends Handler {
        @Override
        public void handleMessage(final android.os.Message msg) {
            if (msg.getData().getString("order").equals("roommate")) {
                try {
                    JSONObject jsonObject = new JSONObject(msg.getData().getString("roommate"));
                    JSONArray jsonArray = new JSONArray(jsonObject.getString("roommate"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = new JSONObject(jsonArray.getString(i));
                        if (object.getString("userId").equals(myId))
                            myChatNo = object.getString("chatNo");
                        if (roommateMap.get(object.getString("userId")) != null) {
                            roommateMap.get(object.getString("userId")).chatNo = object.getInt("chatNo");

                            if (!myId.equals(object.getString("userId"))) {
                                if (object.getString("onOff").equals("on")) {
                                    roommateMap.get(object.getString("userId")).onOff = true;
                                    ll_on.setVisibility(View.VISIBLE);

                                    if (roommateMap.get(object.getString("userId")).iv.getParent() != null)
                                        ((ViewGroup) roommateMap.get(object.getString("userId")).iv.getParent()).removeView(roommateMap.get(object.getString("userId")).iv);
                                    ll_on.addView(roommateMap.get(object.getString("userId")).iv);

                                    roommateMap.get(object.getString("userId")).tv.setVisibility(visibility);

                                    if (roommateMap.get(object.getString("userId")).tv.getParent() != null)
                                        ((ViewGroup) roommateMap.get(object.getString("userId")).tv.getParent()).removeView(roommateMap.get(object.getString("userId")).tv);
                                    ll_on.addView(roommateMap.get(object.getString("userId")).tv);
                                } else {
                                    roommateMap.get(object.getString("userId")).onOff = false;
                                    int index = adapter.getIndex(object.getString("chatNo"));
                                    if (index + 1 < adapter.getCount() && ((Message) adapter.getItem(index + 1)).gubun.equals("state")) {
                                        ((Message) adapter.getItem(index + 1)).iv.remove(roommateMap.get(object.getString("userId")).iv);
                                        ((Message) adapter.getItem(index + 1)).iv.add(roommateMap.get(object.getString("userId")).iv);
                                        ((Message) adapter.getItem(index + 1)).tv.put(roommateMap.get(object.getString("userId")).iv, roommateMap.get(object.getString("userId")).tv);
                                    } else {
                                        Message message = new Message();
                                        message.gubun = "state";
                                        message.iv.add(roommateMap.get(object.getString("userId")).iv);
                                        message.tv.put(roommateMap.get(object.getString("userId")).iv, roommateMap.get(object.getString("userId")).tv);
                                        adapter.addMessage(index + 1, message);
                                    }
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                scrollToBottom(adapter.getIndex(myChatNo));
            }
            if (room.equals(msg.getData().getString("room"))) {
                if (msg.getData().getString("order").equals("sendMsg")) {

                    final Message message = new Message();
                    if (msg.getData().getString("user").equals(myId) || !msg.getData().getString("gubun").equals("message")) {
                        message.name = "";
                        message.profile = "";
                    } else {
                        message.name = roommateMap.get(msg.getData().getString("user")).userName;
                        message.profile = roommateMap.get(msg.getData().getString("user")).profilePicture;
                    }
                    message.chatNo = msg.getData().getString("chatNo");
                    message.userId = msg.getData().getString("user");
                    message.gubun = msg.getData().getString("gubun");
                    message.text = msg.getData().getString("msg");
                    message.dtTm = msg.getData().getString("dtTm");
                    lastChatNo = message.chatNo;
                    adapter.addMessage(message);
                    if (!isBottom && !message.userId.equals(myId))
                        Toast.makeText(context, message.name + " : " + message.text, Toast.LENGTH_SHORT).show();
                } else if (msg.getData().getString("order").equals("state")) {
                    if (roommateMap.get(msg.getData().getString("userId")) != null) {
                        roommateMap.get(msg.getData().getString("userId")).chatNo = Integer.parseInt(msg.getData().getString("chatNo"));

                        if (!myId.equals(msg.getData().getString("userId"))) {
                            if (msg.getData().getString("onOff").equals("on")) {
                                roommateMap.get(msg.getData().getString("userId")).onOff = true;
                                int index = adapter.getIndex(msg.getData().getString("chatNo"));
                                if (index + 1 < adapter.getCount()) {
                                    ((Message) adapter.getItem(index + 1)).iv.remove(roommateMap.get(msg.getData().getString("userId")).iv);
                                    ((Message) adapter.getItem(index + 1)).tv.remove(roommateMap.get(msg.getData().getString("userId")).tv);
                                    adapter.notifyDataSetChanged();
                                    if (((Message) adapter.getItem(index + 1)).iv.size() < 1)
                                        adapter.removeItem(index + 1);
                                }
                                ll_on.setVisibility(View.VISIBLE);
                                if (roommateMap.get(msg.getData().getString("userId")).iv.getParent() != null)
                                    ((ViewGroup) roommateMap.get(msg.getData().getString("userId")).iv.getParent()).removeView(roommateMap.get(msg.getData().getString("userId")).iv);
                                ll_on.addView(roommateMap.get(msg.getData().getString("userId")).iv);

                                roommateMap.get(msg.getData().getString("userId")).tv.setVisibility(visibility);

                                if (roommateMap.get(msg.getData().getString("userId")).tv.getParent() != null)
                                    ((ViewGroup) roommateMap.get(msg.getData().getString("userId")).tv.getParent()).removeView(roommateMap.get(msg.getData().getString("userId")).tv);
                                ll_on.addView(roommateMap.get(msg.getData().getString("userId")).tv);
                            } else {
                                roommateMap.get(msg.getData().getString("userId")).onOff = false;
                                if (roommateMap.get(msg.getData().getString("userId")).marker != null) {
                                    roommateMap.get(msg.getData().getString("userId")).marker.remove();
                                    roommateMap.get(msg.getData().getString("userId")).marker = null;
                                }
                                ll_on.removeView(roommateMap.get(msg.getData().getString("userId")).iv);
                                ll_on.removeView(roommateMap.get(msg.getData().getString("userId")).tv);
                                if (ll_on.getChildCount() < 2) ll_on.setVisibility(View.GONE);

                                int index = adapter.getIndex(msg.getData().getString("chatNo"));
                                if (index + 1 < adapter.getCount() && ((Message) adapter.getItem(index + 1)).gubun.equals("state")) {
                                    ((Message) adapter.getItem(index + 1)).iv.remove(roommateMap.get(msg.getData().getString("userId")).iv);
                                    ((Message) adapter.getItem(index + 1)).iv.add(roommateMap.get(msg.getData().getString("userId")).iv);
                                    ((Message) adapter.getItem(index + 1)).tv.put(roommateMap.get(msg.getData().getString("userId")).iv, roommateMap.get(msg.getData().getString("userId")).tv);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Message message = new Message();
                                    message.gubun = "state";
                                    message.iv.add(roommateMap.get(msg.getData().getString("userId")).iv);
                                    message.tv.put(roommateMap.get(msg.getData().getString("userId")).iv, roommateMap.get(msg.getData().getString("userId")).tv);
                                    adapter.addMessage(index + 1, message);
                                }
                                if (roommateMap.get(msg.getData().getString("userId")).marker != null)
                                    roommateMap.get(msg.getData().getString("userId")).marker.remove();
                            }
                        }
                        adapter.calculate(roommateList);
                    }
                } else if (msg.getData().getString("order").equals("outmate")) {
                    roommateAdapter.removeItem(roommateList.indexOf(roommateMap.get(msg.getData().getString("userId"))));

                    ll_on.removeView(roommateMap.get(msg.getData().getString("userId")).iv);
                    ll_on.removeView(roommateMap.get(msg.getData().getString("userId")).tv);
                    if (ll_on.getChildCount() < 2) ll_on.setVisibility(View.GONE);

                    if (roommateMap.get(msg.getData().getString("userId")).iv.getParent() != null)
                        ((ViewGroup) roommateMap.get(msg.getData().getString("userId")).iv.getParent()).removeView(roommateMap.get(msg.getData().getString("userId")).iv);
                    if (roommateMap.get(msg.getData().getString("userId")).tv.getParent() != null)
                        ((ViewGroup) roommateMap.get(msg.getData().getString("userId")).tv.getParent()).removeView(roommateMap.get(msg.getData().getString("userId")).tv);

                    roommateList.remove(roommateMap.get(msg.getData().getString("userId")));
                    roommateMap.remove(msg.getData().getString("userId"));
                } else if (msg.getData().getString("order").equals("newmate")) {
                    try {
                        JSONArray jsonArray = new JSONArray(msg.getData().getString("roommate"));
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = new JSONObject(jsonArray.getString(i));
                            Roommate roommate = new Roommate(object.getString("userId"));
                            roommate.userName = object.getString("userName");
                            roommate.profilePicture = object.getString("profilePicture");
                            roommate.chatNo = object.getInt("chatNo");
                            roommate.onOff = false;


                            roommate.iv = new ImageView(context);
                            roommate.tv = new TextView(context);
                            roommate.tv.setVisibility(View.GONE);

                            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                            params.weight = 0;
                            roommate.iv.setLayoutParams(params);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.weight = 0;
                            roommate.tv.setLayoutParams(layoutParams);

                            roommate.tv.setText(object.getString("userName"));
                            aq.id(roommate.iv).image(R.drawable.asset55); // icic

                            if (!roommate.profilePicture.equals(""))
                                aq.id(roommate.iv).image(roommate.profilePicture);

//                            ImageHandler handler = new ImageHandler(roommate.iv);
//                            ImageThread thread = new ImageThread(handler, roommate.iv);
//                            thread.start();

                            roommate.viewMap.put(roommate.iv, roommate.tv);
                            roommateList.add(roommate);
                            roommateMap.put(object.getString("userId"), roommate);
                            roommateAdapter.putItems(roommateList);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (msg.getData().getString("order").equals("move")) {
                    if (roommateMap.get(msg.getData().getString("user")).onOff) {
                        ((ChatActivity) getActivity()).setCurrentLocation(msg.getData().getString("user")
                                , msg.getData().getDouble("latitude")
                                , msg.getData().getDouble("longitude"));
                    } else {
                        if (roommateMap.get(msg.getData().getString("user")).marker != null) {
                            roommateMap.get(msg.getData().getString("user")).marker.remove();
                            roommateMap.get(msg.getData().getString("user")).marker = null;
                        }
                    }
                }
            }
        }
    }

    class SendHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            mInputMessageView.setText("");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }


    private void sendMessage() {
        if (mInputMessageView.getText().toString().equals("")) return;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null == activeNetwork) {
            Toast.makeText(context, "네트워크 연결 상태를 확인 하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    JSONArray array = new JSONArray();

                    if (room.equals("") || room.equals("0")) {
                        String[] ids = ((Activity) context).getIntent().getStringExtra("id").split(",");
                        for (int i = 0; i < ids.length; i++) {
                            array.put(i, ids[i]);
                        }

                        String link = context.getString(R.string.ip) + "addRoom.php";
                        String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(array.toString(), "UTF-8");
                        data += "&" + URLEncoder.encode("roomGubun", "UTF-8") + "=" + URLEncoder.encode(roomGubun, "UTF-8");
                        data += "&" + URLEncoder.encode("myId", "UTF-8") + "=" + URLEncoder.encode(myId, "UTF-8");

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

                        JSONObject roomInfo = new JSONObject(sb.toString());
                        room = roomInfo.getString("roomId");

                        JSONArray roommateInfo = new JSONArray(roomInfo.getString("roommate"));
                        for (int i = 0; i < roommateInfo.length(); i++) {
                            JSONObject object = roommateInfo.getJSONObject(i);

                            roommateList.add(i + 1, new Roommate(object.getString("userId")));
                            roommateList.get(i + 1).userName = object.getString("userName");
                            roommateList.get(i + 1).profilePicture = object.getString("profilePicture");
                            roommateMap.put(object.getString("userId"), roommateList.get(i + 1));
                        }

                        jsonObject.put("order", "createRoom");
                        jsonObject.put("friends", array);
                    } else {
                        jsonObject.put("order", "sendMsg");
                    }
                    jsonObject.put("gubun", "message");
                    jsonObject.put("room", room);
                    jsonObject.put("msg", mInputMessageView.getText().toString());

                    SharedPreferences.Editor editor = context.getSharedPreferences("now", 0).edit();
                    editor.putString("room", room);
                    editor.commit();

                    out.writeUTF(jsonObject.toString());
                    sendHandler.sendEmptyMessage(1);
                } catch (SocketException e) {
                    System.out.println("예외:" + e);
                    System.out.println("##접속중인 서버와 연결이 끊어졌습니다.");
                    return;
                } catch (IOException e) {
                    System.out.println("예외:" + e);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void scrollToBottom(final int index) {
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(index);
            }
        });
//        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private String encodeImage(String path) {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imagefile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

    private Bitmap decodeImage(String data) {
        byte[] b = Base64.decode(data, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
        return bmp;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            context.unregisterReceiver(receiver);
            SharedPreferences.Editor editor = context.getSharedPreferences("now", 0).edit();
            editor.remove("room");
            editor.commit();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public void outRoom() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("order", "outRoom");
                    out.writeUTF(jsonObject.toString());

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("DELETE FROM chat WHERE roomId = '").append(room).append("'");
                    dbHelper.edit(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("DELETE FROM roommate WHERE roomId = '").append(room).append("'");
                    dbHelper.edit(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("DELETE FROM room WHERE roomId = '").append(room).append("'");
                    dbHelper.edit(stringBuilder.toString());

                    getActivity().finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void getChat(String roomId, String userId, String chatNo) {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String myJSON) {
                JSONObject jsonObject = null;
                JSONObject object = null;
                JSONArray jsonArray = null;
                try {
                    jsonObject = new JSONObject(myJSON);
                    jsonArray = new JSONArray(jsonObject.getString("chat"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        object = jsonArray.getJSONObject(i);
                        if (adapter.getItem(object.getString("chatNo")) == null) {
                            Message message = new Message();
                            if (roommateMap.get(object.getString("userId")) != null) {
                                message.name = roommateMap.get(object.getString("userId")).userName;
                                message.profile = roommateMap.get(object.getString("userId")).profilePicture;
                            }
                            message.chatNo = object.getString("chatNo");
                            message.userId = object.getString("userId");
                            message.gubun = object.getString("gubun");
                            message.text = object.getString("message");
                            message.dtTm = object.getString("dtTm");

                            adapter.addMessage(message);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String roomId = (String) params[0];
                    String userId = (String) params[1];
                    String chatNo = (String) params[2];

                    String link = getString(R.string.ip) + "getMessage.php";
                    String data = URLEncoder.encode("roomId", "UTF-8") + "=" + URLEncoder.encode(roomId, "UTF-8");
                    data += "&" + URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    data += "&" + URLEncoder.encode("chatNo", "UTF-8") + "=" + URLEncoder.encode(chatNo, "UTF-8");

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
                    return sb.toString().trim();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }

        GetDataJSON task = new GetDataJSON();
        task.execute(roomId, userId, chatNo);
    }
}