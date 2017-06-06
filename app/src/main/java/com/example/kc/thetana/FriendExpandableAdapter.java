package com.example.kc.thetana;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by kc on 2017-02-19.
 */

public class FriendExpandableAdapter extends BaseExpandableListAdapter {
    ArrayList<FriendGroup> group = new ArrayList<FriendGroup>();
    ImageHelper imageHelper;

    FriendExpandableAdapter() {

    }

    public void setGroup(ArrayList<FriendGroup> friendGroup) {
        group.clear();
        group = friendGroup;
        notifyDataSetChanged();
    }
    public void addGroup(FriendGroup friendGroup) {
        group.add(friendGroup);
        notifyDataSetChanged();
    }
    public void addChild(int index, FriendChild friendChild) {
        group.get(index).friendChildren.add(friendChild);
        notifyDataSetChanged();
    }

    public void clearGroup() {
        group.clear();
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return group.get(groupPosition).friendChildren.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return group.get(groupPosition).friendChildren.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView tv_name = null;
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_group, parent, false);
        tv_name = (TextView) convertView.findViewById(R.id.group_tv_name);
        tv_name.setText(group.get(groupPosition).groupName);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView tv_name = null;
        TextView tv_state = null;
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_child, parent, false);
        final AQuery aq = new AQuery(parent.getContext());
        final ImageView iv_profile = (ImageView) convertView.findViewById(R.id.child_iv_profile);

        tv_name = (TextView) convertView.findViewById(R.id.child_tv_name);
        tv_state = (TextView) convertView.findViewById(R.id.child_tv_state);
        tv_name.setText(group.get(groupPosition).friendChildren.get(childPosition).name);
        tv_state.setText(group.get(groupPosition).friendChildren.get(childPosition).state);

        if (!group.get(groupPosition).friendChildren.get(childPosition).profile.equals(""))
            aq.id(iv_profile).image(group.get(groupPosition).friendChildren.get(childPosition).profile);

//        ImageView imageView = new ImageView(parent.getContext());
//        if (!group.get(groupPosition).friendChildren.get(childPosition).profile.equals(""))
//            aq.id(imageView).image(group.get(groupPosition).friendChildren.get(childPosition).profile);
//        ImageHandler handler = new ImageHandler(imageView, iv_profile);
//        ImageThread thread = new ImageThread(handler, imageView);
//        thread.start();

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
