package ru.spbau.shevchenko.chatbattle.frontend;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;


public class MessageAdapter extends BaseAdapter implements View.OnClickListener {
    final private Context context;
    final private ArrayList<Message> messages;

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

    private void setStatusVisibility(MessageViewHolder holder, int loadingVisibility, int actionButtonsVisibility) {
        holder.loadingBar.setVisibility(loadingVisibility);
        holder.deleteBtn.setVisibility(actionButtonsVisibility);
        holder.retryBtn.setVisibility(actionButtonsVisibility);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder;
        if (convertView == null) {
            LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.message, null);
            holder = new MessageViewHolder((TextView) convertView.findViewById(R.id.message_sender),
                    (TextView) convertView.findViewById(R.id.message_body),
                    (ImageView) convertView.findViewById(R.id.message_image),
                    (ProgressBar) convertView.findViewById(R.id.delivering_progress_bar),
                    (ImageButton) convertView.findViewById(R.id.delete_message_btn),
                    (ImageButton) convertView.findViewById(R.id.retry_sending_btn));
            holder.deleteBtn.setTag(position+1); // TODO: WHY +1??????
            holder.deleteBtn.setOnClickListener(this);
            holder.retryBtn.setOnClickListener(this);
            holder.retryBtn.setTag(position+1);
            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }
        Message message = messages.get(position);
        holder.textView.setText(message.getText());
        holder.senderView.setText(String.format(Locale.getDefault(), "%d", message.getAuthorId()));
        holder.imageView.setImageResource(android.R.color.transparent);
        convertView.setBackgroundColor(0xFFFFFFFF);
        switch (message.getStatus()) {
            case DELIVERED: {
                setStatusVisibility(holder, View.GONE, View.GONE);
                break;
            }
            case SENDING: {
                setStatusVisibility(holder, View.VISIBLE, View.GONE);
                break;
            }
            case FAILED: {
                setStatusVisibility(holder, View.GONE, View.VISIBLE);
                convertView.setBackgroundColor(0xFFFFAAAA);
                break;
            }
        }
        if (!message.getTag().isEmpty()) {
            Uri whiteboardURI = ChatService.getWhiteboardURI(message.getTag(), new RequestCallback(){
                @Override
                public void run(RequestResult requestResult) {
                    notifyDataSetChanged();
                }
            });
            if (whiteboardURI == null) {
                holder.imageView.setImageResource(R.drawable.grey_square);
            }
            else {
                holder.imageView.setImageURI(whiteboardURI);
            }
        }
        return convertView;
    }

    public int add(Message message) {
        messages.add(message);
        notifyDataSetChanged();
        return messages.size() - 1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_message_btn: {
                int position = (int) v.getTag();
                messages.remove(position);
                notifyDataSetChanged();
                break;
            }
            case R.id.retry_sending_btn: {
                int position = (int) v.getTag();
                Message message = messages.get(position);
                message.setStatus(Message.Status.SENDING);
                // TODO: complete
                notifyDataSetChanged();
                break;
            }
        }
    }

    private static class MessageViewHolder {
        final private TextView senderView;
        final private TextView textView;
        final private ImageView imageView;
        final private ProgressBar loadingBar;
        private final ImageButton deleteBtn;
        private final ImageButton retryBtn;

        MessageViewHolder(TextView senderView, TextView textView, ImageView imageView,
                          ProgressBar loadingBar, ImageButton deleteBtn, ImageButton retryBtn) {
            this.senderView = senderView;
            this.textView = textView;
            this.imageView = imageView;
            this.loadingBar = loadingBar;
            this.deleteBtn = deleteBtn;
            this.retryBtn = retryBtn;
        }
    }
}
