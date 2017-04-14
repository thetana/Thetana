package com.example.kc.thetana;

import android.graphics.Bitmap;

/**
 * Created by kc on 2017-02-25.
 */

public class Message {

    public static final int TYPE_FMESSAGE = 0;
    public static final int TYPE_MMESSAGE = 1;
    public static final int TYPE_FIMAGE = 2;
    public static final int TYPE_MIMAGE = 3;

    public int mType;
    private String mMessage;
    private String mUser;
    private String mChatNo;
    private Bitmap mImage;

    private Message() {
    }

    public int getType() {
        return mType;
    }

    public String getMessage() {
        return mMessage;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public String getUser() {
        return mUser;
    }

    public String getChatNo() {
        return mChatNo;
    }

    public static class Builder {
        private final int mType;
        private Bitmap mImage;
        private String mMessage;
        private String mUser;
        private String mChatNo;

        public Builder(int type) {
            mType = type;
        }

        public Builder image(Bitmap image) {
            mImage = image;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }
        public Builder user(String user) {
            mUser = user;
            return this;
        }
        public Builder number(Integer number) {
            return this;
        }
        public Builder chatNo(String chatNo) {
            mChatNo = chatNo;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mImage = mImage;
            message.mMessage = mMessage;
            message.mUser = mUser;
            message.mChatNo = mChatNo;
            return message;
        }
    }
}