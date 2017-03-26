package com.example.kc.thetana;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

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
    private OnFragmentInteractionListener mListener;
    private List<Message> mMessages = new ArrayList<Message>();
    private Hashtable<String, Integer> mHashtable = new Hashtable<String, Integer>();
    private MessageAdapter mAdapter;
    SendHandler sendHandler;
    ChatThread chatThread;
    ChatHandler handler;
    String ServerIP = "35.163.3.139";
    Socket socket;
    DataOutputStream out;
    String room = "", myId = "", roomGubun = "", myName = "";
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        dbHelper = new DBHelper(context, "thetana.db", null, 1);
        myId = context.getSharedPreferences("user", 0).getString("id", "");
        myName = context.getSharedPreferences("user", 0).getString("name", "");
        room = ((Activity) context).getIntent().getStringExtra("roomId");
        roomGubun = ((Activity) context).getIntent().getStringExtra("roomGubun");

//        myId = context.getSharedPreferences("user", 0).getString("id", "");
//        roommate.put(myId, myId);
//        String[] ids = ((Activity) context).getIntent().getStringExtra("id").split(",");
//        for(int i = 0; i < ids.length; i++){
//            roommate.put(ids[i], ids[i]);
//        }

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
    }

    @Override
    public void onStart() {
        super.onStart();

        class InsertData extends AsyncTask<String, Void, String> {
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    JSONArray chats = new JSONObject(s).getJSONArray("chat");
                    for (int i = 0; i < chats.length(); i++) {
                        JSONObject chat = chats.getJSONObject(i);
                        StringBuilder stringBuilder = new StringBuilder();

                        if(dbHelper.chatCount(chat.getString("chatId")) > 0){
                            stringBuilder.append("UPDATE chat set readed = ").append(chat.getString("readed"));
                            stringBuilder.append(", updateDt = ").append(chat.getString("updateDt")).append(", ");
                            stringBuilder.append("WHERE chatId = ").append(chat.getString("chatId"));
                        }else {
                            stringBuilder.append("INSERT INTO chat");
                            stringBuilder.append(" VALUES(").append(chat.getString("chatId")).append(", ");
                            stringBuilder.append(chat.getString("chatNo")).append(", ");
                            stringBuilder.append(chat.getString("roomId")).append(", '");
                            stringBuilder.append(chat.getString("userId")).append("', '");
                            stringBuilder.append(chat.getString("gubun")).append("', '");
                            stringBuilder.append(chat.getString("message")).append("', ");
                            stringBuilder.append(chat.getString("readed")).append(", '");
                            stringBuilder.append(chat.getString("insertDt")).append("', '");
                            stringBuilder.append(chat.getString("updateDt")).append("')");
                        }
                        dbHelper.edit(stringBuilder.toString());
                    }
                    SharedPreferences preferences = context.getSharedPreferences("update", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("chat", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date()));
                    editor.commit();

                    boolean isUpdate = true;
                    String chatNo = "";
                    int lastChatNo = 0;
                    JSONObject jsonObj = dbHelper.getChat(room);
                    JSONArray jsonArray = jsonObj.getJSONArray("chat");
                    String name = "";
                    for (int i = 0; i < jsonArray.length(); i++) {
                        int type = 0;
                        JSONObject object = jsonArray.getJSONObject(i);
                        if(object.getString("userId").equals(myId)) type = Message.TYPE_MMESSAGE;
                        else type = Message.TYPE_FMESSAGE;

                        try {
                            JSONObject jsonObject = new JSONObject(context.getSharedPreferences("friend", 0).getString(object.getString("userId"), ""));
                            name = jsonObject.getString("userName");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (isUpdate && object.getInt("chatNo") > context.getSharedPreferences("chatNo", 0).getInt(room, 99999)){
                            chatNo = object.getString("chatNo");
                            isUpdate = false;
                        }
                        addMessage(name, object.getString("message"), object.getString("chatNo"), object.getInt("readed"), type);
                        lastChatNo = object.getInt("chatNo");
                    }

                    if(!chatNo.equals("")) updateRead(chatNo);

                    if(lastChatNo > 0) {
                        preferences = context.getSharedPreferences("chatNo", 0);
                        editor = preferences.edit();
                        editor.putInt(room, lastChatNo);
                        editor.commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String link = getString(R.string.ip) + "getChat.php";
                    String data = URLEncoder.encode("roomId", "UTF-8") + "=" + URLEncoder.encode(room, "UTF-8");
                    data += "&" + URLEncoder.encode("updateDt", "UTF-8") + "=" + URLEncoder.encode(context.getSharedPreferences("update", 0).getString("chat", ""), "UTF-8");

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
        InsertData task = new InsertData();
        task.execute();




//        try {
//            boolean isUpdate = true;
//            String chatNo = "";
//            int lastChatNo = 0;
//            JSONObject jsonObj = dbHelper.getChat(room);
//            JSONArray jsonArray = jsonObj.getJSONArray("chat");
//            String name = "";
//            for (int i = 0; i < jsonArray.length(); i++) {
//                int type = 0;
//                JSONObject object = jsonArray.getJSONObject(i);
//                if(object.getString("userId").equals(myId)) type = Message.TYPE_MMESSAGE;
//                else type = Message.TYPE_FMESSAGE;
//
//                try {
//                    JSONObject jsonObject = new JSONObject(context.getSharedPreferences("friend", 0).getString(object.getString("userId"), ""));
//                    name = jsonObject.getString("userName");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                if (isUpdate && object.getInt("chatNo") > context.getSharedPreferences("chatNo", 0).getInt(room, 99999)){
//                    chatNo = object.getString("chatNo");
//                    isUpdate = false;
//                }
//                addMessage(name, object.getString("message"), object.getString("chatNo"), object.getInt("readed"), type);
//                lastChatNo = object.getInt("chatNo");
//            }
//
//            if(!chatNo.equals("")) updateRead(chatNo);
//
//            if(lastChatNo > 0) {
//                SharedPreferences preferences = context.getSharedPreferences("chatNo", 0);
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putInt(room, lastChatNo);
//                editor.commit();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    class ChatHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (room.equals(msg.getData().getString("room"))) {
                if(msg.getData().getString("order").equals("sendMsg")) {
                    String name = "";
                    int type = 0;
                    if (msg.getData().getString("user").equals(myId)) type = Message.TYPE_MMESSAGE;
                    else type = Message.TYPE_FMESSAGE;
                    try {
                        JSONObject jsonObject = new JSONObject(context.getSharedPreferences("friend", 0).getString(msg.getData().getString("user"), ""));
                        name = jsonObject.getString("userName");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    addMessage(name, msg.getData().getString("msg"), msg.getData().getString("chatNo"), msg.getData().getInt("readed"), type);
                }
                else if(msg.getData().getString("order").equals("readMsg")) {
                    readMessage(msg.getData().getString("chatNo"));
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageAdapter(mMessages, mHashtable);
        context = activity;
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

//                    Iterator<String> iterator = roommate.keySet().iterator();
//                    int i = 0;
//                    while (iterator.hasNext()){
//                        array.put(i + 1, iterator.next());
//                        i++;
//                    } // 친구 맵 관리

                    if (room.equals("")) {
                        array.put(0, myId);
                        String[] ids = ((Activity) context).getIntent().getStringExtra("id").split(",");
                        for (int i = 0; i < ids.length; i++) {
                            array.put(i + 1, ids[i]);
                        }

                        String link = context.getString(R.string.ip) + "addRoom.php";
                        String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(array.toString(), "UTF-8");
                        data += "&" + URLEncoder.encode("roomGubun", "UTF-8") + "=" + URLEncoder.encode(roomGubun, "UTF-8");

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
                            sb.append(json);
                            break;
                        }

                        room = sb.toString();

                        globalClass = new GlobalClass(context);
                        globalClass.updateRooms();

                        jsonObject.put("order", "createRoom");
                        jsonObject.put("friends", array);
                    } else {
                        jsonObject.put("order", "sendMsg");
                    }
                    jsonObject.put("gubun", "message");
                    jsonObject.put("room", room);
                    jsonObject.put("msg", mInputMessageView.getText().toString());
                    sendFCM(mInputMessageView.getText().toString());
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

    private void sendFCM(final String msg) {
        class InsertData extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String message = (String) params[0];

                    String link = context.getString(R.string.ip) + "sendFCM.php";
                    String data = URLEncoder.encode("title", "UTF-8") + "=" + URLEncoder.encode(myName, "UTF-8");
                    data += "&" + URLEncoder.encode("roomId", "UTF-8") + "=" + URLEncoder.encode(room, "UTF-8");
                    data += "&" + URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(myId, "UTF-8");
                    data += "&" + URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
                    data += "&" + URLEncoder.encode("gubun", "UTF-8") + "=" + URLEncoder.encode(roomGubun, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(msg);
    }

    private void updateRead(String chatNo) {
        JSONObject jsonObject = new JSONObject();
        class UpdateData extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String link = context.getString(R.string.ip) + "putChat.php";
                    String data = URLEncoder.encode("roomId", "UTF-8") + "=" + URLEncoder.encode(room, "UTF-8");
                    data += "&" + URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(myId, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        UpdateData task = new UpdateData();
        task.execute();

        try {
            jsonObject.put("order", "readMsg");
            jsonObject.put("room", room);
            jsonObject.put("chatNo", chatNo);
            out.writeUTF(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        mAdapter.read(chatNo);
        mAdapter.notifyItemInserted(0);
        scrollToBottom();
    }

    private void addMessage(String id, String message, String chatNo, int readed, int type) {
        mHashtable.put(chatNo, mMessages.size());
        mMessages.add(new Message.Builder(type).message(message).user(id).chatNo(chatNo).number(readed).build());

        mAdapter = new MessageAdapter(mMessages, mHashtable);
        mAdapter.notifyItemInserted(0);
        scrollToBottom();
    }

    private void addImage(Bitmap bmp) {
        mHashtable.put("", mMessages.size());
        mMessages.add(new Message.Builder(Message.TYPE_FIMAGE)
                .image(bmp).build());
        mAdapter = new MessageAdapter(mMessages, mHashtable);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}