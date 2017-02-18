package ru.spbau.shevchenko.chatbattle.frontend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
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

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

@SuppressWarnings("WeakerAccess")
public class MessageAdapter extends BaseAdapter implements View.OnClickListener {

    private static final int DRAWABLE_PADDING_DP = 0;
    private static final int PADDING_FOR_SPINNER_DP = 30;

    final private Context context;
    final private ArrayList<Message> messages;

    MessageAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    private enum Alert {

        RETRY, DELETE;

        public int getId() {
            switch (this) {
                case RETRY:
                    return R.id.alert_retry;
                case DELETE:
                    return R.id.alert_delete;
            }
            throw new IllegalArgumentException();
        }

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

    public void add(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alert: {
                showPopup(v);
                break;
            }
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MessageViewHolder holder;
        final Message message = messages.get(position);
        boolean isCurPlayer = message.getAuthorId() == ProfileManager.getPlayer().getId();
        if (convertView == null || isCurPlayer != ((MessageViewHolder) convertView.getTag()).isCurPlayer) {
            final LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.message, null);
            holder = new MessageViewHolder((TextView) convertView.findViewById(R.id.message_body),
                    (ProgressBar) convertView.findViewById(R.id.delivering_progress_bar),
                    (ImageButton) convertView.findViewById(R.id.alert),
                    isCurPlayer
            );
            holder.alertBtn.setOnClickListener(this);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
            isCurPlayer = holder.isCurPlayer;
        }
        holder.alertBtn.setTag(position);

        holder.textView.setBackgroundResource(((ChatActivity) context).getPlayerColor(message.getAuthorId()).getTextViewId());

        if (message.getText().isEmpty()) {
            holder.textView.setTextSize(0);
        }
        else{
            holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.message_text_size));
        }
        holder.textView.setText(message.getText());

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x;
        @SuppressWarnings("NumericCastThatLosesPrecision")
        int padding = (int) context.getResources().getDimension(R.dimen.activity_horizontal_margin);
        holder.textView.setMaxWidth(displayWidth - 2 * padding - dpAsPixels(PADDING_FOR_SPINNER_DP));

        setLayoutParams(holder.textView, isCurPlayer, RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_LEFT);

        setVisibility(message, holder);
        setTextViewDrawable(message, holder);
        convertView.setTag(holder);
        return convertView;
    }

    private void setVisibility(Message message, MessageViewHolder holder) {
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
    }

    private void setLayoutParams(View view, boolean isCurPlayer, int isCurAttribute, int isNotCurAttribute) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        lp.addRule(isCurPlayer ? isCurAttribute : isNotCurAttribute);
        view.setLayoutParams(lp);
    }

    private void setTextViewDrawable(Message message, MessageViewHolder holder) {
        if (!message.getTag().isEmpty()) {
            final Uri whiteboardURI = ChatService.getWhiteboardURI(message.getTag(), new RequestCallback() {
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
                    Log.e("setTextViewDrawable", "can't open input stream");
                }
                pic = Drawable.createFromStream(inputStream, whiteboardURI.toString());
            } else {
                pic = ContextCompat.getDrawable(context, R.drawable.grey_square);
            }
            holder.textView.setCompoundDrawablePadding(DRAWABLE_PADDING_DP);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, pic);
        } else {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    private void setStatusVisibility(MessageViewHolder holder, int loadingVisibility, int actionButtonsVisibility) {
        holder.loadingBar.setVisibility(loadingVisibility);
        holder.alertBtn.setVisibility(actionButtonsVisibility);
    }

    private void showPopup(final View v) {
        final Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        final PopupMenu popup = new PopupMenu(wrapper, v);
        popup.inflate(R.menu.popup_alert);
        for (Alert alert : Alert.values()) {
            final MenuItem menuItem = popup.getMenu().findItem(alert.getId());
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
                        final Message message = messages.get(position);
                        String whiteboardEncoded = "";
                        if (!message.getTag().isEmpty()) {
                            final byte[] whiteboardBytes;
                            try {
                                File whiteboardFile = new File(MyApplication.storageDir, message.getTag());
                                FileInputStream whiteboardInStream = new FileInputStream(whiteboardFile);

                                /**
                                 * Ilya claims that size of file can not be very big, so
                                 * these warnings refer to impossible situations.
                                 */

                                //noinspection NumericCastThatLosesPrecision
                                whiteboardBytes = new byte[(int) whiteboardFile.length()];
                                //noinspection ResultOfMethodCallIgnored
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
                        messages.remove((int) v.getTag());
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
        //noinspection NumericCastThatLosesPrecision
        return (int) (sizeInDp * scale + 0.5f);
    }

    private static class MessageViewHolder {

        private final TextView textView;
        private final ProgressBar loadingBar;
        private final ImageButton alertBtn;
        private final boolean isCurPlayer;

        MessageViewHolder(TextView textView, ProgressBar loadingBar,
                          ImageButton alertBtn, boolean isCur) {
            this.textView = textView;
            this.loadingBar = loadingBar;
            this.alertBtn = alertBtn;
            this.isCurPlayer = isCur;
        }
    }

}
