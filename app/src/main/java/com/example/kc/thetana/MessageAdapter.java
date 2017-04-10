package com.example.kc.thetana;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private ArrayList<Roommate> roommateList = new ArrayList<Roommate>();
    private int[] mUsernameColors;

    public MessageAdapter(List<Message> messages, ArrayList<Roommate> roommate) {
        mMessages = messages;
        roommateList = roommate;
        this.notifyDataSetChanged();

        //  mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    public void clearList() {
        mMessages.clear();
        this.notifyDataSetChanged();
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Message.TYPE_FMESSAGE:
                layout = R.layout.item_message_friend;
                break;
            case Message.TYPE_MMESSAGE:
                layout = R.layout.item_message_me;
                break;
            case Message.TYPE_FIMAGE:
                layout = R.layout.item_message_friend;
                break;
            case Message.TYPE_MIMAGE:
                layout = R.layout.item_message_me;
                break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Message message = mMessages.get(position);
        viewHolder.setMessage(message.getMessage());
        viewHolder.setName(message.getUser());
        viewHolder.setNumber(Integer.parseInt(message.getChatNo()));
        viewHolder.setImage(message.getImage());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

//    public void read(String chatNo) {
//        if (mHashtable.get(chatNo) != null && mHashtable.get(chatNo) > 0) {
//            for (int i = mHashtable.get(chatNo); i < mMessages.size(); i++) {
//                mMessages.get(i).mNumber--;
//            }
//        }
//    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView tv_name;
        private TextView tv_message;
        private TextView tv_number;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            switch (viewType) {
                case Message.TYPE_FMESSAGE:
                    tv_name = (TextView) itemView.findViewById(R.id.fmsg_tv_name);
                    tv_message = (TextView) itemView.findViewById(R.id.fmsg_tv_message);
                    tv_number = (TextView) itemView.findViewById(R.id.fmsg_tv_number);
                    break;
                case Message.TYPE_MMESSAGE:
                    tv_message = (TextView) itemView.findViewById(R.id.mymsg_tv_message);
                    tv_number = (TextView) itemView.findViewById(R.id.mymsg_tv_number);
                    break;
                case Message.TYPE_FIMAGE:
                    tv_name = (TextView) itemView.findViewById(R.id.fmsg_tv_name);
                    tv_message = (TextView) itemView.findViewById(R.id.fmsg_tv_message);
                    break;
                case Message.TYPE_MIMAGE:
                    tv_message = (TextView) itemView.findViewById(R.id.mymsg_tv_message);
                    break;
            }
        }

        public void setMessage(String message) {
            if (null == tv_message) return;
            if (null == message) return;
            tv_message.setText(message);
        }

        public void setName(String name) {
            if (null == tv_name) return;
            if (null == name) return;
            tv_name.setText(name);
        }

        public void setNumber(Integer chatNo) {
            if (null == tv_number) return;
            else {
                int number = 0;
                for (int i = 0; i < roommateList.size(); i++) {
                    if (!roommateList.get(i).onOff) {
                        if (roommateList.get(i).chatNo < chatNo) {
                            number++;
                        }
                    }
                }
                if (1 > number) tv_number.setText("");
                else tv_number.setText(String.valueOf(number));
            }
        }

        public void setImage(Bitmap bmp) {
            if (null == mImageView) return;
            if (null == bmp) return;
            mImageView.setImageBitmap(bmp);
        }

        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }
    }
}