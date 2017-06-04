package com.example.kc.thetana;

import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

/**
 * Created by kc on 2017-04-08.
 */

public class Roommate {
    public String userId = "";
    public int chatNo = 0;
    public Boolean onOff = false;
    public String userName = "";
    public String profilePicture = "";
    public ImageView iv;
    public TextView tv;
    public HashMap<ImageView, TextView> viewMap = new HashMap<ImageView, TextView>();
    public Marker marker = null;
    public Roommate(String userId) {
        this.userId = userId;

    }
}
