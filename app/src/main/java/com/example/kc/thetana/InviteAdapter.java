package com.example.kc.thetana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;

/**
 * Created by kc on 2017-03-05.
 */

public class InviteAdapter extends BaseAdapter {
    private ArrayList<InviteItem> inviteItems = new ArrayList<InviteItem>() ;
    TextView tv_name;
    CheckBox cb_check;
    ImageView iv_profile;
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
        final AQuery aq = new AQuery(parent.getContext());
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_invite, parent, false);

        tv_name = (TextView) convertView.findViewById(R.id.invite_tv_name);
        cb_check = (CheckBox) convertView.findViewById(R.id.invite_cb_check);
        iv_profile = (ImageView) convertView.findViewById(R.id.invite_iv_profile);

        tv_name.setText(inviteItems.get(position).name);
        cb_check.setChecked(inviteItems.get(position).checked);

        if (!inviteItems.get(position).profile.equals(""))
            aq.id(iv_profile).image(inviteItems.get(position).profile);

//        ImageView imageView = new ImageView(parent.getContext());
//        if (!inviteItems.get(position).profile.equals(""))
//            aq.id(imageView).image(inviteItems.get(position).profile);
//        ImageHandler handler = new ImageHandler(imageView, iv_profile);
//        ImageThread thread = new ImageThread(handler, imageView);
//        thread.start();

        return convertView;
    }
}
