package com.example.kc.thetana;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkReceiver extends BroadcastReceiver {
    private ChatFragment chatFragment;

    public NetworkReceiver() {
        super();
    }

    public NetworkReceiver(ChatFragment chatFragment) {
        this.chatFragment = chatFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                NetworkInfo _wifi_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (_wifi_network != null) {
                    if (_wifi_network != null && activeNetInfo != null) {
                        if (chatFragment.socket == null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        chatFragment.socket = new Socket(chatFragment.ServerIP, 9999);
                                        chatFragment.out = new DataOutputStream(chatFragment.socket.getOutputStream());
                                        chatFragment.out.writeUTF(chatFragment.myId);
                                        chatFragment.out.writeUTF(chatFragment.room);
                                        chatFragment.chatThread = new ChatThread(chatFragment.handler, chatFragment.socket);
                                        chatFragment.chatThread.start();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                            JSONObject jsonObject = null;
                            JSONObject object = null;
                            JSONArray jsonArray = null;
                            try {
                                jsonObject = chatFragment.dbHelper.getChat(chatFragment.room);
                                jsonArray = jsonObject.getJSONArray("chat");
                                chatFragment.adapter.clearMessage();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    object = jsonArray.getJSONObject(i);
                                    Message message = new Message();
                                    if (chatFragment.roommateMap.get(object.getString("userId")) != null)
                                        message.name = chatFragment.roommateMap.get(object.getString("userId")).userName;
                                    message.chatNo = object.getString("chatNo");
                                    message.userId = object.getString("userId");
                                    message.gubun = object.getString("gubun");
                                    message.profile = object.getString("profilePicture");
                                    message.text = object.getString("message");
                                    message.dtTm = object.getString("dtTm");

                                    chatFragment.adapter.addMessage(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (chatFragment.socket != null) {
                            chatFragment.socket.close();
                            chatFragment.socket = null;
                        }
                        for (int i = 1; chatFragment.roommateList.size() > i; i++) {
                            if (chatFragment.roommateList.get(i).onOff) {
                                chatFragment.roommateList.get(i).chatNo = Integer.parseInt(chatFragment.lastChatNo);
                                chatFragment.roommateList.get(i).onOff = false;
                                chatFragment.ll_on.removeView(chatFragment.roommateList.get(i).iv);
                                chatFragment.ll_on.removeView(chatFragment.roommateList.get(i).tv);
                                if (chatFragment.ll_on.getChildCount() < 2) chatFragment.ll_on.setVisibility(View.GONE);

                                int index = chatFragment.adapter.getIndex(String.valueOf(chatFragment.roommateList.get(i).chatNo));
                                if (index + 1 < chatFragment.adapter.getCount() && ((Message) chatFragment.adapter.getItem(index + 1)).gubun.equals("state")) {
                                    ((Message) chatFragment.adapter.getItem(index + 1)).iv.remove(chatFragment.roommateList.get(i).iv);
                                    ((Message) chatFragment.adapter.getItem(index + 1)).iv.add(chatFragment.roommateList.get(i).iv);
                                    ((Message) chatFragment.adapter.getItem(index + 1)).tv.put(chatFragment.roommateList.get(i).iv, chatFragment.roommateList.get(i).tv);
                                    chatFragment.adapter.notifyDataSetChanged();
                                } else {
                                    Message message = new Message();
                                    message.gubun = "state";
                                    message.iv.add(chatFragment.roommateList.get(i).iv);
                                    message.tv.put(chatFragment.roommateList.get(i).iv, chatFragment.roommateList.get(i).tv);
                                    chatFragment.adapter.addMessage(index + 1, message);
                                }
                            }
                            chatFragment.adapter.calculate(chatFragment.roommateList);
                        }
                    }
                }
            } catch (Exception e) {
                Log.i("ULNetworkReceiver", e.getMessage());
            }
        }
    }
}
