package com.example.kc.thetana;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import java.util.ArrayList;
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
    private OnFragmentInteractionListener mListener;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    SendHandler sendHandler;
    ChatThread chatThread;
    ChatHandler handler;
    String ServerIP = "192.168.244.128";
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
                    String s = myId;
                    out.writeUTF(s);
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
        try {
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

                addMessage(name, object.getString("message"), type);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class ChatHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (room.equals(msg.getData().getString("room"))) {
                String name = "";
                int type = 0;
                if(msg.getData().getString("user").equals(myId)) type = Message.TYPE_MMESSAGE;
                else type = Message.TYPE_FMESSAGE;
                try {
                    JSONObject jsonObject = new JSONObject(context.getSharedPreferences("friend", 0).getString(msg.getData().getString("user"), ""));
                    name = jsonObject.getString("userName");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                addMessage(name, msg.getData().getString("msg"), type);
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageAdapter(mMessages);
        context = activity;
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/

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

                        String link = "http://192.168.244.128/addRoom.php";
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

//        String message = mInputMessageView.getText().toString().trim();
//        mInputMessageView.setText("");
//        //addMessage(message);
//        JSONObject sendText = new JSONObject();
//        try {
//            sendText.put("text", message);
//            //socket.emit("message", sendText);
//        } catch (JSONException e) {
//
//        }
    }

    private void sendFCM(final String msg) {

        class InsertData extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                try {
                    String message = (String) params[0];

                    String link = "http://192.168.244.128/sendFCM.php";
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

    private void addMessage(String id, String message, int type) {
        mMessages.add(new Message.Builder(type).message(message).user(id).build());

        mAdapter = new MessageAdapter(mMessages);
        mAdapter.notifyItemInserted(0);
        scrollToBottom();
    }

    private void addImage(Bitmap bmp) {
        mMessages.add(new Message.Builder(Message.TYPE_FIMAGE)
                .image(bmp).build());
        mAdapter = new MessageAdapter(mMessages);
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
        //socket.disconnect();
    }

}