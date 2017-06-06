package com.example.kc.thetana;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

/**
 * Created by kc on 2017-06-06.
 */

public class ImageThread extends Thread {
    Handler handler;
    ImageView imageView;
    public ImageThread(Handler handler, ImageView imageView) {
        this.imageView = imageView;
        this.handler = handler;
    }

    @Override
    public void run() {
        boolean isOk = true;
        while (isOk){
            if(imageView.getDrawable() != null) {
                android.os.Message message = new android.os.Message();
                Bundle bundle = new Bundle();
                message.setData(bundle);
                handler.sendMessage(message);
                isOk = false;
            }
        }
    }
}
