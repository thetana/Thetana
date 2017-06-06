package com.example.kc.thetana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;

/**
 * Created by kc on 2017-04-25.
 */

public class RoommateAdapter extends BaseAdapter {
    ArrayList<Roommate> roommates = new ArrayList<Roommate>();
    ImageView iv_profile;
    TextView tv_name;

    public void putItems(ArrayList<Roommate> itmes) {
        roommates = itmes;
        notifyDataSetChanged();
    }
    public void removeItem(int index) {
        roommates.remove(index);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return roommates.size();
    }

    @Override
    public Object getItem(int position) {
        return roommates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AQuery aq = new AQuery(parent.getContext());
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_roommate, parent, false);

        iv_profile = (ImageView) convertView.findViewById(R.id.roommate_iv_profile);
        tv_name = (TextView) convertView.findViewById(R.id.roommate_tv_name);
        tv_name.setText(roommates.get(position).userName);
        if (!roommates.get(position).profilePicture.equals(""))
            aq.id(iv_profile).image(roommates.get(position).profilePicture);

        return convertView;
    }
}
