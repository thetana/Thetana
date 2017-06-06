package com.example.kc.thetana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;

/**
 * Created by kc on 2017-03-04.
 */

public class RoomAdapter extends BaseAdapter {

    private ArrayList<RoomItem> roomItems = new ArrayList<RoomItem>();
    TextView tv_name, tv_dtTm, tv_number;
    ImageView iv_pictrue;

    public void setItem(ArrayList<RoomItem> itme) {
        roomItems = itme;
        notifyDataSetChanged();
    }

    public void addItem(RoomItem item) {
        roomItems.add(item);
        notifyDataSetChanged();
    }
    public void clearItem() {
        roomItems.clear();
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
        final AQuery aq = new AQuery(parent.getContext());
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_room, parent, false);

        tv_name = (TextView) convertView.findViewById(R.id.itemRoom_tv_name);
        tv_dtTm = (TextView) convertView.findViewById(R.id.itemRoom_tv_dtTm);
        tv_number = (TextView) convertView.findViewById(R.id.itemRoom_tv_number);
        iv_pictrue = (ImageView) convertView.findViewById(R.id.itemRoom_iv_pictrue);

        tv_name.setText(roomItems.get(position).name);
        tv_dtTm.setText(roomItems.get(position).dtTm);
        tv_number.setText(roomItems.get(position).number);
//        if (!roomItems.get(position).pictrue.equals(""))
//            aq.id(iv_pictrue).image(roomItems.get(position).pictrue);

        if (!roomItems.get(position).pictrue.equals(""))
            aq.id(iv_pictrue).image(roomItems.get(position).pictrue);

//        ImageView imageView = new ImageView(parent.getContext());
//        if (!roomItems.get(position).pictrue.equals(""))
//            aq.id(imageView).image(roomItems.get(position).pictrue);
//        ImageHandler handler = new ImageHandler(imageView, iv_pictrue);
//        ImageThread thread = new ImageThread(handler, imageView);
//        thread.start();

        return convertView;
    }
}
