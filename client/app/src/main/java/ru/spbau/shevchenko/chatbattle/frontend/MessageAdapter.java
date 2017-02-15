package ru.spbau.shevchenko.chatbattle.frontend;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static java.lang.Math.min;


public class MessageAdapter extends BaseAdapter implements View.OnClickListener {

    private enum Alert {
        RETRY, DELETE;

        public int getId() {
            switch (this) {
                case RETRY: return R.id.alert_retry;
                case DELETE: return R.id.alert_delete;
            }
            throw new IllegalArgumentException();
        }


    }
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
        holder.alertBtn.setVisibility(actionButtonsVisibility);
    }

    private static final int MARGIN_SMALL_DP = 5;
    private static final int MARGIN_BIG_DP = 40;
    private final Collection<View> setDelta = new HashSet<>();



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MessageViewHolder holder;
        final Message message = messages.get(position);
        boolean isCur = message.getAuthorId() == ProfileManager.getPlayer().getId();
        if (convertView == null || isCur != ((MessageViewHolder) convertView.getTag()).isCur) {
            isCur = message.getAuthorId() == ProfileManager.getPlayer().getId();
            LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.message, null);
            holder = new MessageViewHolder((TextView) convertView.findViewById(R.id.message_body),
                    (ImageView) convertView.findViewById(R.id.message_image),
                    (ProgressBar) convertView.findViewById(R.id.delivering_progress_bar),
                    (ImageButton) convertView.findViewById(R.id.alert),
                    isCur
            );
            holder.alertBtn.setOnClickListener(this);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
            isCur = holder.isCur;
        }
        holder.alertBtn.setTag(position);


        holder.textView.setBackgroundResource(((ChatActivity) context).getPlayerColor(message.getAuthorId()).getTextViewId());
        holder.textView.setText(message.getText());

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.textView.getLayoutParams();
        lp.addRule(isCur ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
        if (isCur) {
            lp.setMargins(0, 0, 0, dpAsPixels(MARGIN_SMALL_DP));
        }
        else {
            lp.setMargins(0, 0, MARGIN_BIG_DP, dpAsPixels(MARGIN_SMALL_DP));
        }
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
        Log.d("add", message.getText());
        messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alert: {
                showPopup(v);
            }
        }
    }

    private void showPopup(final View v) {
        Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        PopupMenu popup = new PopupMenu(wrapper, v);
        popup.inflate(R.menu.popup_alert);
        for (Alert alert : Alert.values()) {
            MenuItem menuItem = popup.getMenu().findItem(alert.getId());
            final SpannableStringBuilder s = new SpannableStringBuilder(menuItem.getTitle());
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)), 0, s.length(), 0);
            s.setSpan(new RelativeSizeSpan(1.2f), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            menuItem.setTitle(s);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.alert_retry: {
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
                                break;
                            } catch (IOException e) {
                                Log.e("retry_sending", "Error while reading data");
                                break;
                            }
                        }
                        message.setStatus(Message.Status.SENDING);
                        ((ChatActivity) context).sendMessage(message, whiteboardEncoded);

                        notifyDataSetChanged();
                        break;
                    }
                    case R.id.alert_delete: {
                        int position = (int) v.getTag();
                        Log.d("delete_message", String.valueOf(position) + " - " + messages.size());
                        messages.remove(position);
                        notifyDataSetChanged();
                        break;
                    }
                }
                return true;
            }
        });
        popup.show();
    }



    private int dpAsPixels(int sizeInDp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeInDp*scale + 0.5f);
    }

    private static class MessageViewHolder {

        final private TextView textView;
        final private ImageView imageView;
        final private ProgressBar loadingBar;
        private final ImageButton alertBtn;
        private final boolean isCur;

        MessageViewHolder(TextView textView, ImageView imageView,
                          ProgressBar loadingBar, ImageButton alertBtn,
                          boolean isCur) {
            this.textView = textView;
            this.imageView = imageView;
            this.loadingBar = loadingBar;
            this.alertBtn = alertBtn;
            this.isCur = isCur;
        }
    }


}
