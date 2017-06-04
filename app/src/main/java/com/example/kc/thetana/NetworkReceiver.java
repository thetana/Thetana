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


                            chatFragment.getChat(chatFragment.room, chatFragment.myId, chatFragment.lastChatNo);
                        }
                    } else {
                        if (chatFragment.socket != null) {
                            chatFragment.socket.close();
                            chatFragment.socket = null;
                        }
                    }
                }
            } catch (Exception e) {
                Log.i("ULNetworkReceiver", e.getMessage());
            }
        }
    }
}
