package com.example.kc.thetana;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        if (data.get("what").equals("msg")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("INSERT INTO chat VALUES(null, ");
            stringBuilder.append(data.get("chatNo")).append(", ");
            stringBuilder.append(data.get("roomId")).append(", '");
            stringBuilder.append(data.get("userId")).append("', '");
            stringBuilder.append(data.get("gubun")).append("', '");
            stringBuilder.append(data.get("message")).append("')");
            dbHelper.edit(stringBuilder.toString());

//        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
//        if (!ChatActivity.class.getName().equals(componentName.getClassName())) sendNotification(data);
            if (!getSharedPreferences("now", 0).getString("room", "").equals(data.get("roomId")))
                sendNotification(data);
        } else if (data.get("what").equals("updateUser")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE friend SET userName = '").append(data.get("name"));
            stringBuilder.append("', stateMessage = '").append(data.get("stateMessage"));
            stringBuilder.append("', phoneNumber = '").append(data.get("phoneNumber"));
            stringBuilder.append("' WHERE friendId = '").append(data.get("userId")).append("'");
            dbHelper.edit(stringBuilder.toString());

            stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE roommate SET userName = '").append(data.get("name"));
            stringBuilder.append("', stateMessage = '").append(data.get("stateMessage"));
            stringBuilder.append("' WHERE userId = '").append(data.get("userId")).append("'");
            dbHelper.edit(stringBuilder.toString());
        } else if (data.get("what").equals("updateProfile")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE friend SET profilePicture = '").append(data.get("profile"));
            stringBuilder.append("' WHERE friendId = '").append(data.get("userId")).append("'");
            dbHelper.edit(stringBuilder.toString());

            stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE roommate SET profilePicture = '").append(data.get("profile"));
            stringBuilder.append("' WHERE userId = '").append(data.get("userId")).append("'");
            dbHelper.edit(stringBuilder.toString());
        } else if (data.get("what").equals("updateBackground")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE friend SET backgroundPhoto = '").append(data.get("background"));
            stringBuilder.append("' WHERE friendId = '").append(data.get("userId")).append("'");
            dbHelper.edit(stringBuilder.toString());

            stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE roommate SET backgroundPhoto = '").append(data.get("background"));
            stringBuilder.append("' WHERE userId = '").append(data.get("userId")).append("'");
            dbHelper.edit(stringBuilder.toString());
        } else if (data.get("what").equals("newRoom")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("INSERT INTO room VALUES(").append(data.get("roomId")).append(", '");
            stringBuilder.append(data.get("title")).append("', '");
            stringBuilder.append(data.get("subTitle")).append("', '");
            stringBuilder.append(data.get("roomGubun")).append("')");
            dbHelper.edit(stringBuilder.toString());

            try {
                JSONArray jsonArray = new JSONArray(data.get("roommate"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("INSERT INTO roommate VALUES(null, ");
                    stringBuilder.append(object.getString("roomId")).append(", '");
                    stringBuilder.append(object.getString("userId")).append("', '");
                    stringBuilder.append(object.getString("userName")).append("', '");
                    stringBuilder.append(object.getString("stateMessage")).append("', '");
                    stringBuilder.append(object.getString("profilePicture")).append("', '");
                    stringBuilder.append(object.getString("backgroundPhoto")).append("')");
                    dbHelper.edit(stringBuilder.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (data.get("what").equals("newRoommate")) {
            try {
                JSONArray jsonArray = new JSONArray(data.get("roommate"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("INSERT INTO roommate VALUES(null, ");
                    stringBuilder.append(object.getString("roomId")).append(", '");
                    stringBuilder.append(object.getString("userId")).append("', '");
                    stringBuilder.append(object.getString("userName")).append("', '");
                    stringBuilder.append(object.getString("stateMessage")).append("', '");
                    stringBuilder.append(object.getString("profilePicture")).append("', '");
                    stringBuilder.append(object.getString("backgroundPhoto")).append("')");
                    dbHelper.edit(stringBuilder.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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