package com.example.kc.thetana;

import android.os.Bundle;
import android.os.Handler;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by kc on 2017-03-01.
 */

public class ChatThread extends Thread {
    Handler handler;
    Socket socket;
    DataInputStream in;

    public ChatThread(Handler handler, Socket socket) {
        this.socket = socket;
        this.handler = handler;
        try {
            in = new DataInputStream(this.socket.getInputStream());
        } catch (Exception e) {
            System.out.println("예외:" + e);
        }
    }

    @Override
    public void run() {
        try {
            while (in != null) {
                String msg = in.readUTF();
                JSONObject jsonObject = new JSONObject(msg);
                android.os.Message message = new android.os.Message();
                Bundle bundle = new Bundle();
                bundle.putString("order", jsonObject.getString("order"));
                if (jsonObject.getString("order").equals("sendMsg")) {
                    bundle.putString("user", jsonObject.getString("user"));
                    bundle.putString("msg", jsonObject.getString("msg"));
                    bundle.putString("room", jsonObject.getString("room"));
                    bundle.putString("chatNo", jsonObject.getString("chatNo"));
                    bundle.putString("gubun", jsonObject.getString("gubun"));
                    bundle.putString("dtTm", jsonObject.getString("dtTm"));
                } else if (jsonObject.getString("order").equals("roommate")) {
                    bundle.putString("roommate", msg);
                } else if (jsonObject.getString("order").equals("state")) {
                    bundle.putString("room", jsonObject.getString("room"));
                    bundle.putString("userId", jsonObject.getString("userId"));
                    bundle.putString("onOff", jsonObject.getString("onOff"));
                    bundle.putString("chatNo", jsonObject.getString("chatNo"));
                } else if (jsonObject.getString("order").equals("outmate")) {
                    bundle.putString("room", jsonObject.getString("room"));
                    bundle.putString("userId", jsonObject.getString("userId"));
                } else if (jsonObject.getString("order").equals("newmate")) {
                    bundle.putString("room", jsonObject.getString("room"));
                    bundle.putString("roommate", jsonObject.getString("roommate"));
                } else if (jsonObject.getString("order").equals("move")) {
                    bundle.putString("room", jsonObject.getString("room"));
                    bundle.putString("user", jsonObject.getString("user"));
                    bundle.putDouble("latitude", jsonObject.getDouble("latitude"));
                    bundle.putDouble("longitude", jsonObject.getDouble("longitude"));
                }

                message.setData(bundle);
                handler.sendMessage(message);

            }
        } catch (SocketException e) {
            System.out.println("예외:" + e);
            System.out.println("##접속중인 서버와 연결이 끊어졌습니다.");
            return;

        } catch (Exception e) {
            System.out.println("예외:" + e);

        }
    }
}
