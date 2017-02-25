package com.example.kc.thetana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kc on 2017-02-19.
 */

public class FriendExpandableAdapter extends BaseExpandableListAdapter {
    ArrayList<FriendGroup> group = new ArrayList<FriendGroup>();

    FriendExpandableAdapter(){

    }

    public void setGroup(ArrayList<FriendGroup> friendGroup) {
        group = friendGroup;
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

        tv_name = (TextView) convertView.findViewById(R.id.child_tv_name);
        tv_state = (TextView) convertView.findViewById(R.id.child_tv_state);
        tv_name.setText(group.get(groupPosition).friendChildren.get(childPosition).name);
        tv_state.setText(group.get(groupPosition).friendChildren.get(childPosition).state);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
