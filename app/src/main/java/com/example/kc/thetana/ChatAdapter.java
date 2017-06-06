package com.example.kc.thetana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by kc on 2017-05-03.
 */

public class ChatAdapter extends BaseAdapter {
    private ArrayList<Message> messages = new ArrayList<Message>();
    private HashMap<String, Message> msgMap = new HashMap<String, Message>();

    private LinearLayout ll_chat;
    private ImageView iv_profile;
    private TextView tv_name, tv_message, tv_number, tv_dtTm;

    public void calculate(ArrayList<Roommate> roommateList) {
        for (int j = 0; j < messages.size(); j++) {
            Message message = messages.get(j);
            if (message.gubun.equals("message") && message.visibility == View.VISIBLE) {
                if (message.chatNo.equals("")) return;
                int mChatNo = Integer.parseInt(message.chatNo);
                int count = 0;
                for (int i = 0; i < roommateList.size(); i++) {
                    if (!roommateList.get(i).onOff && roommateList.get(i).chatNo < mChatNo) {
                        count++;
                    }
                }
                if (count == 0) message.number = "다 읽음.";
                else message.number = String.valueOf(count) + "명 안읽음.";
            }
        }
        notifyDataSetChanged();
    }

    public void addMessage(Message item) {
//        if(!item.chatNo.equals("") && msgMap.get(item.chatNo) != null) return;
        messages.add(item);
        msgMap.put(messages.get(messages.size() - 1).chatNo, messages.get(messages.size() - 1));
        notifyDataSetChanged();
    }

    public void addMessage(int index, Message item) {
//        if(!item.chatNo.equals("") && msgMap.get(item.chatNo) != null) return;
        messages.add(index, item);
        msgMap.put(messages.get(index).chatNo, messages.get(index));
        notifyDataSetChanged();
    }

    public void removeItem(int index) {
        msgMap.remove(messages.get(index).chatNo);
        messages.remove(index);
        notifyDataSetChanged();
    }

    public void removeItem(String chatNo) {
        messages.remove(msgMap.get(chatNo));
        msgMap.remove(chatNo);
        notifyDataSetChanged();
    }

    public void clearMessage() {
        messages.clear();
        msgMap.clear();
        notifyDataSetChanged();
    }

    public Message getItem(String chatNo) {
        return msgMap.get(chatNo);
    }

    public int getIndex(String chatNo) {
        return messages.indexOf(msgMap.get(chatNo));
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        final AQuery aq = new AQuery(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String myId = context.getSharedPreferences("user", 0).getString("id", "");
        if (messages.get(position).gubun.equals("message")) {
            String dtTm = "";
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(messages.get(position).dtTm);
                dtTm = new SimpleDateFormat("yyyy년 MM월 dd일 E요일 HH시 mm분 ss초").format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (messages.get(position).userId.equals(myId)) {
                convertView = inflater.inflate(R.layout.item_message_me, parent, false);
                tv_message = (TextView) convertView.findViewById(R.id.mymsg_tv_message);
                tv_number = (TextView) convertView.findViewById(R.id.mymsg_tv_number);
                tv_dtTm = (TextView) convertView.findViewById(R.id.mymsg_tv_dtTm);

                tv_message.setText(messages.get(position).text);
                tv_number.setText(messages.get(position).number);
                tv_dtTm.setText(dtTm);
                tv_number.setVisibility(messages.get(position).visibility);
                tv_dtTm.setVisibility(messages.get(position).visibility);
            } else {
                convertView = inflater.inflate(R.layout.item_message_friend, parent, false);
                tv_name = (TextView) convertView.findViewById(R.id.fmsg_tv_name);
                tv_message = (TextView) convertView.findViewById(R.id.fmsg_tv_message);
                tv_number = (TextView) convertView.findViewById(R.id.fmsg_tv_number);
                tv_dtTm = (TextView) convertView.findViewById(R.id.fmsg_tv_dtTm);
                iv_profile = (ImageView) convertView.findViewById(R.id.fmsg_iv_profile);

                tv_message.setText(messages.get(position).text);
                tv_name.setText(messages.get(position).name);
                tv_number.setText(messages.get(position).number);
                tv_dtTm.setText(dtTm);
                tv_number.setVisibility(messages.get(position).visibility);
                tv_dtTm.setVisibility(messages.get(position).visibility);
                if (!messages.get(position).profile.equals(""))
                    aq.id(iv_profile).image(messages.get(position).profile);
            }
        }
        else if (messages.get(position).gubun.equals("invite") || messages.get(position).gubun.equals("out")) {
            convertView = inflater.inflate(R.layout.item_system, parent, false);
            tv_message = (TextView) convertView.findViewById(R.id.system_tv_mag);
            tv_message.setText(messages.get(position).text);
        } else if (messages.get(position).gubun.equals("state")) {
            convertView = inflater.inflate(R.layout.item_state, parent, false);
            ll_chat = (LinearLayout) convertView.findViewById(R.id.state_ll_chat);
            for (int i = 0; i < messages.get(position).iv.size(); i++) {
                ImageView iv = messages.get(position).iv.get(i);
                TextView tv = messages.get(position).tv.get(iv);

                if (iv != null) {
                    if (iv.getParent() != null)
                        ((ViewGroup) iv.getParent()).removeView(iv);
                    ll_chat.addView(iv);
                }

                if (tv != null) {
                    if (tv.getParent() != null)
                        ((ViewGroup) tv.getParent()).removeView(tv);
                    ll_chat.addView(tv);
                    tv.setVisibility(messages.get(position).visibility);
                }
            }
        }

        return convertView;
    }
}
