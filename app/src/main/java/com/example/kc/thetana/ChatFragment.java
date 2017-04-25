package com.example.kc.thetana;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

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
    GlobalClass globalClass;

    private EditText mInputMessageView;
    private RecyclerView mMessagesView;
    private List<Message> mMessages = new ArrayList<Message>();
    public ArrayList<Roommate> roommateList = new ArrayList<Roommate>();
    public Hashtable<String, Roommate> roommateMap = new Hashtable<String, Roommate>();
    private Hashtable<String, Integer> mHashtable = new Hashtable<String, Integer>();
    private MessageAdapter mAdapter;
    RoommateAdapter roommateAdapter = new RoommateAdapter();
    SendHandler sendHandler;
    ChatThread chatThread;
    ChatHandler handler;
    String ServerIP = "35.163.3.139";
    Socket socket;
    DataOutputStream out;
    String room = "", myId = "", roomGubun = "", myName = "", myStateMsg, myProfile, myBackground;
    int myChatNo = 0;
    DBHelper dbHelper;
//    HashMap<String, String> roommate  = new HashMap<String, String>(); // 친구맵

    public ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageAdapter(mMessages, roommateList);
        context = activity;

        dbHelper = new DBHelper(context, "thetana.db", null, 1);
        myId = context.getSharedPreferences("user", 0).getString("id", "");
        myName = context.getSharedPreferences("user", 0).getString("name", "");
        myProfile = context.getSharedPreferences("user", 0).getString("profilePicture", "");
        room = ((Activity) context).getIntent().getStringExtra("roomId");
        roomGubun = ((Activity) context).getIntent().getStringExtra("roomGubun");

        JSONObject jsonObject = dbHelper.getRoommate(room);
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("roommate");

            roommateList.add(0, new Roommate(myId));
            roommateList.get(0).userName = myName;
            roommateList.get(0).profilePicture = myProfile;
            roommateMap.put(myId, roommateList.get(0));
            for (int i = 1; i <= jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i - 1);
                roommateList.add(i, new Roommate(object.getString("userId")));
                roommateList.get(i).userName = object.getString("userName");
                roommateList.get(i).profilePicture = object.getString("profilePicture");
                roommateMap.put(object.getString("userId"), roommateList.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        JSONObject jsonObject = null;
        JSONObject object = null;
        JSONArray jsonArray = null;
        try {
            jsonObject = dbHelper.getChat(room);
            jsonArray = jsonObject.getJSONArray("chat");
            for (int i = 0; i < jsonArray.length(); i++) {
                object = jsonArray.getJSONObject(i);
                String chatNo = object.getString("chatNo");
                String userId = object.getString("userId");
                String gubun = object.getString("gubun");
                String message = object.getString("message");
                String userName = object.getString("userName");
                String profilePicture = object.getString("profilePicture");
//                String userName = roommateMap.get(object.getString("userId")).userName;

                int type = 0;
                if (userId.equals(myId)) type = Message.TYPE_MMESSAGE;
                else type = Message.TYPE_FMESSAGE;

                addMessage(userName, profilePicture, message, chatNo, type);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = context.getSharedPreferences("now", 0).edit();
        editor.putString("room", room);
        editor.commit();
        roommateAdapter = new RoommateAdapter();
        roommateAdapter.putItems(roommateList);
        ((ChatActivity)getActivity()).lv_roommate.setAdapter(roommateAdapter);
    }

    class ChatHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.getData().getString("order").equals("roommate")) {
                try {
                    JSONObject jsonObject = new JSONObject(msg.getData().getString("roommate"));
                    JSONArray jsonArray = new JSONArray(jsonObject.getString("roommate"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = new JSONObject(jsonArray.getString(i));
                        if (object.getString("userId").equals(myId))
                            myChatNo = object.getInt("chatNo");
                        roommateMap.get(object.getString("userId")).chatNo = object.getInt("chatNo");
                        if (object.getString("onOff").equals("on"))
                            roommateMap.get(object.getString("userId")).onOff = true;
                        else roommateMap.get(object.getString("userId")).onOff = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (room.equals(msg.getData().getString("room"))) {
                if (msg.getData().getString("order").equals("sendMsg")) {
                    String name = "", profilePicture = "";
                    int type = 0;
                    if (msg.getData().getString("user").equals(myId)) type = Message.TYPE_MMESSAGE;
                    else type = Message.TYPE_FMESSAGE;
                    name = roommateMap.get(msg.getData().getString("user")).userName;
                    profilePicture = roommateMap.get(msg.getData().getString("user")).profilePicture;
                    addMessage(name, profilePicture, msg.getData().getString("msg"), msg.getData().getString("chatNo"), type);
                } else if (msg.getData().getString("order").equals("state")) {
                    roommateMap.get(msg.getData().getString("userId")).chatNo = Integer.parseInt(msg.getData().getString("chatNo"));
                    if (msg.getData().getString("onOff").equals("on"))
                        roommateMap.get(msg.getData().getString("userId")).onOff = true;
                    else roommateMap.get(msg.getData().getString("userId")).onOff = false;
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
                            roommateList.add(roommate);
                            roommateMap.put(object.getString("userId"), roommate);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        mInputMessageView = (EditText) view.findViewById(R.id.message_input);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        if (mInputMessageView.getText().toString().equals("")) return;

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

    public void sendImage(String path) {
        JSONObject sendData = new JSONObject();
        try {
            sendData.put("image", encodeImage(path));
            Bitmap bmp = decodeImage(sendData.getString("image"));
            addImage(bmp);
            //ocket.emit("message", sendData);
        } catch (JSONException e) {

        }
    }

    private void readMessage(String chatNo) {
//        mAdapter.read(chatNo);
        mAdapter.notifyItemInserted(0);
        scrollToBottom();
    }

    private void addMessage(String id, String profile, String message, String chatNo, int type) {
        mHashtable.put(chatNo, mMessages.size());
        mMessages.add(new Message.Builder(type).message(message).user(id).profile(profile).chatNo(chatNo).build());

        mAdapter = new MessageAdapter(mMessages, roommateList);
        mAdapter.notifyItemInserted(0);
        scrollToBottom();
    }

    private void addImage(Bitmap bmp) {
        mHashtable.put("", mMessages.size());
        mMessages.add(new Message.Builder(Message.TYPE_FIMAGE)
                .image(bmp).build());
        mAdapter = new MessageAdapter(mMessages, roommateList);
        mAdapter.notifyItemInserted(0);
        scrollToBottom();
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
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

        SharedPreferences.Editor editor = context.getSharedPreferences("now", 0).edit();
        editor.remove("room");
        editor.commit();

        try {
            socket.close();
        } catch (IOException e) {
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
}