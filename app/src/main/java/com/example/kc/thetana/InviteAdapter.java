package com.example.kc.thetana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kc on 2017-03-05.
 */

public class InviteAdapter extends BaseAdapter {
    private ArrayList<InviteItem> inviteItems = new ArrayList<InviteItem>() ;
    TextView tv_name;
    CheckBox cb_check;
    public void setItem(ArrayList<InviteItem> itme ){
        inviteItems = itme;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return inviteItems.size();
    }

    @Override
    public Object getItem(int position) {
        return inviteItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_invite, parent, false);

        tv_name = (TextView) convertView.findViewById(R.id.invite_tv_name);
        cb_check = (CheckBox) convertView.findViewById(R.id.invite_cb_check);

        tv_name.setText(inviteItems.get(position).name);
        cb_check.setChecked(inviteItems.get(position).checked);

        return convertView;
    }
}
