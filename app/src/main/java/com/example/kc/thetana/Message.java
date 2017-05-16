package com.example.kc.thetana;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kc on 2017-02-25.
 */

public class Message {

    public String text = "";
    public String name = "";
    public String chatNo = "";
    public String profile = "";
    public String gubun = "";
    public String userId = "";
    public String number = "";
    public String dtTm = "";
    public int visibility = View.GONE;
    public ArrayList<ImageView> iv = new ArrayList<ImageView>();
    public HashMap<ImageView, TextView> tv = new HashMap<ImageView, TextView>();
}