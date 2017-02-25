package com.example.kc.thetana;

import java.util.ArrayList;

/**
 * Created by kc on 2017-02-19.
 */

public class FriendGroup {
    ArrayList<FriendChild> friendChildren = new ArrayList<FriendChild>();
    String groupName;

    FriendGroup(String name){
        groupName = name;
    }
}
