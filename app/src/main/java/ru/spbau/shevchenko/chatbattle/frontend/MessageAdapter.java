package ru.spbau.shevchenko.chatbattle.frontend;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;

/**
 * Created by Nikolay on 31.10.16.
 */

public class MessageAdapter extends BaseAdapter {
    Context context;
    ArrayList<Message> messages;

    MessageAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder;
        if (convertView == null) {
            LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.message, null);
            holder = new MessageViewHolder();
            holder.senderView = (TextView) convertView.findViewById(R.id.message_sender);
            holder.textView = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder)convertView.getTag();
        }
        Message message = messages.get(position);
        holder.textView.setText(message.getText());
        holder.senderView.setText(message.getSender());
        return convertView;
    }

    public void add(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    private static class MessageViewHolder {
        public TextView senderView;
        public TextView textView;
    }
}
