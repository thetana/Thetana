package com.example.kc.thetana;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by kc on 2017-06-06.
 */

public class ImageHelper {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        int size = 0;
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xFFFFFFFF;
        final Paint paint = new Paint();
        if(bitmap.getWidth() >= bitmap.getHeight()) size = bitmap.getWidth();
        else if(bitmap.getHeight() >= bitmap.getWidth()) size = bitmap.getHeight();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 255, 255, 255);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
