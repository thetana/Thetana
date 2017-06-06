package com.example.kc.thetana;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.widget.ImageView;

/**
 * Created by kc on 2017-06-06.
 */

public class ImageHandler extends Handler {
    ImageView imageView, tempImage;
    ImageHelper imageHelper;
    public ImageHandler(ImageView imageView) {
        this.imageView = imageView;
        this.tempImage = imageView;
    }
    public ImageHandler(ImageView tempImage, ImageView imageView) {
        this.imageView = imageView;
        this.tempImage = tempImage;
    }
    @Override
    public void handleMessage(android.os.Message msg) {
        imageHelper = new ImageHelper();
        if(tempImage.getDrawable() != null) {
            tempImage.setImageBitmap(imageHelper.getRoundedCornerBitmap(((BitmapDrawable) tempImage.getDrawable()).getBitmap()));
            imageView.setImageBitmap(((BitmapDrawable) tempImage.getDrawable()).getBitmap());
        }
    }
}
