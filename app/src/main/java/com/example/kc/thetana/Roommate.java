package com.example.kc.thetana;

/**
 * Created by kc on 2017-04-08.
 */

public class Roommate {
    public String userId = "";
    public int chatNo = 0;
    public Boolean onOff = false;
    public Integer readed = 0;
    public String userName = "";
    public String stateMessage = "";
    public String profilePicture = "";
    public String backgroundPhoto = "";

    public Roommate(String userId) {
        this.userId = userId;
    }
}
