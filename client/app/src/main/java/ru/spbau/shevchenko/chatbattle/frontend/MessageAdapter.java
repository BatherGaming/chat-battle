package ru.spbau.shevchenko.chatbattle.frontend;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
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

    private static final int MARGIN_SMALL_DP = 5;
    private static final int MARGIN_BIG_DP = 20;


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MessageViewHolder holder;
        final Message message = messages.get(position);
        boolean isCur = message.getAuthorId() == ProfileManager.getPlayer().getId();
        if (convertView == null || isCur != ((MessageViewHolder) convertView.getTag()).isCur) {
            isCur = message.getAuthorId() == ProfileManager.getPlayer().getId();
            LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.message, null);
            holder = new MessageViewHolder((TextView) convertView.findViewById(R.id.message_body),
                    (ImageView) convertView.findViewById(R.id.message_image),
                    (ProgressBar) convertView.findViewById(R.id.delivering_progress_bar),
                    (ImageButton) convertView.findViewById(R.id.delete_message_btn),
                    (ImageButton) convertView.findViewById(R.id.retry_sending_btn),
                    isCur
            );
            holder.deleteBtn.setOnClickListener(this);
            holder.retryBtn.setOnClickListener(this);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
            isCur = holder.isCur;
        }
        holder.deleteBtn.setTag(position);
        holder.retryBtn.setTag(position);
        holder.textView.setBackgroundResource(((Chat) context).getPlayerColor(message.getAuthorId()).getTextViewId());
        holder.textView.setText(message.getText());

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.textView.getLayoutParams();
        lp.addRule(isCur ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
        if (isCur)
            lp.setMargins(dpAsPixels(MARGIN_BIG_DP), 0, dpAsPixels(MARGIN_SMALL_DP), dpAsPixels(MARGIN_SMALL_DP));
        else
            lp.setMargins(dpAsPixels(MARGIN_SMALL_DP), 0, dpAsPixels(MARGIN_BIG_DP), dpAsPixels(MARGIN_SMALL_DP));
        holder.textView.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
        lp.addRule(isCur ? RelativeLayout.ALIGN_RIGHT : RelativeLayout.ALIGN_LEFT, holder.textView.getId());
        holder.imageView.setLayoutParams(lp);
        holder.imageView.setImageResource(android.R.color.transparent);

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
            Drawable pic;
            if (whiteboardURI != null) {
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(whiteboardURI);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                pic = Drawable.createFromStream(inputStream, whiteboardURI.toString());
            } else {
                pic = ContextCompat.getDrawable(context, R.drawable.grey_square);
            }
            holder.textView.setCompoundDrawablePadding(2);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, pic);

        } else {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        }
        convertView.setTag(holder);
        return convertView;
    }

    public void add(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_message_btn: {
                int position = (int) v.getTag();
                Log.d("delete_message", String.valueOf(position) + " - " + messages.size());
                messages.remove(position);
                notifyDataSetChanged();
                break;
            }
            case R.id.retry_sending_btn: {
                int position = (int) v.getTag();
                Message message = messages.get(position);
                String whiteboardEncoded = "";
                if (!message.getTag().isEmpty()) {
                    byte[] whiteboardBytes;
                    try {
                        File whiteboardFile = new File(MyApplication.storageDir, message.getTag());
                        FileInputStream whiteboardInStream = new FileInputStream(whiteboardFile);
                        whiteboardBytes = new byte[(int) whiteboardFile.length()];
                        whiteboardInStream.read(whiteboardBytes);
                        whiteboardEncoded = Base64.encodeToString(whiteboardBytes, Base64.NO_WRAP);
                    } catch (FileNotFoundException e) {
                        Log.e("retry_sending", "Whiteboard file not found");
                        return;
                    } catch (IOException e) {
                        Log.e("retry_sending", "Error while reading data");
                        return;
                    }
                }
                message.setStatus(Message.Status.SENDING);
                ((Chat) context).sendMessage(message, whiteboardEncoded);

                notifyDataSetChanged();
                break;
            }
        }
    }

    private int dpAsPixels(int sizeInDp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeInDp*scale + 0.5f);
    }

    private static class MessageViewHolder {

        final private TextView textView;
        final private ImageView imageView;
        final private ProgressBar loadingBar;
        private final ImageButton deleteBtn;
        private final ImageButton retryBtn;
        private final boolean isCur;

        MessageViewHolder(TextView textView, ImageView imageView,
                          ProgressBar loadingBar, ImageButton deleteBtn, ImageButton retryBtn,
                          boolean isCur) {
            this.textView = textView;
            this.imageView = imageView;
            this.loadingBar = loadingBar;
            this.deleteBtn = deleteBtn;
            this.retryBtn = retryBtn;
            this.isCur = isCur;
        }
    }
}
