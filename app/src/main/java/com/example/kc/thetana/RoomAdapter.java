package com.example.kc.thetana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kc on 2017-03-04.
 */

public class RoomAdapter extends BaseAdapter {

    private ArrayList<RoomItem> roomItems = new ArrayList<RoomItem>() ;
    TextView tv_name, tv_msg, tv_dtTm, tv_number;
    Button bt_add;

    public void setItem(ArrayList<RoomItem> itme ){
        roomItems = itme;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return roomItems.size();
    }

    @Override
    public Object getItem(int position) {
        return roomItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_room, parent, false);

        tv_name = (TextView) convertView.findViewById(R.id.itemRoom_tv_name);
        tv_msg = (TextView) convertView.findViewById(R.id.itemRoom_tv_msg);
        tv_dtTm = (TextView) convertView.findViewById(R.id.itemRoom_tv_dtTm);
        tv_number = (TextView) convertView.findViewById(R.id.itemRoom_tv_number);

        tv_name.setText(roomItems.get(position).name);
        tv_msg.setText(roomItems.get(position).msg);
        tv_dtTm.setText(roomItems.get(position).dtTm);
        tv_number.setText(roomItems.get(position).number);

        return convertView;
    }
}
