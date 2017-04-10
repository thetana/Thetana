package com.example.kc.thetana;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by kc on 2017-03-08.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    DBHelper dbHelper = new DBHelper(this, "thetana.db", null, 1);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (data.size() > 0) {
            Log.d(TAG, "Message data payload: " + data);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO chat VALUES(null, ");
        stringBuilder.append(data.get("chatNo")).append(", ");
        stringBuilder.append(data.get("roomId")).append(", '");
        stringBuilder.append(data.get("userId")).append("', '");
        stringBuilder.append(data.get("gubun")).append("', '");
        stringBuilder.append(data.get("message" )).append("')");
        dbHelper.edit(stringBuilder.toString());

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
        if (!ChatActivity.class.getName().equals(componentName.getClassName())) sendNotification(data);
    }

    private void sendNotification(Map<String, String> messageBody) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("roomId", messageBody.get("roomId"));
        intent.putExtra("roomGubun", messageBody.get("gubun"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(dbHelper.getRoommateName(messageBody.get("userId")))
                .setContentText(messageBody.get("message"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}