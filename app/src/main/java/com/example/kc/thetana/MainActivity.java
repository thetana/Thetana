package com.example.kc.thetana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import java.net.URISyntaxException;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class MainActivity extends AppCompatActivity {
    SharedPreferences preferences;

//    private Socket socket;
//    {
//        try{
//            socket = IO.socket("http://192.168.244.128:3000/");
//        }catch(URISyntaxException e){
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        socket.connect();

        preferences = getSharedPreferences("user", 0);
        String id = preferences.getString("id", "");
        if (TextUtils.isEmpty(id) || id.equals(null) || id.equals("null")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);

            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
        }
    }
}
