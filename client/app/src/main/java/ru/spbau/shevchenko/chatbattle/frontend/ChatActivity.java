package ru.spbau.shevchenko.chatbattle.frontend;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.spbau.shevchenko.chatbattle.Message;
import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ChatService;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;


public class ChatActivity extends BasicActivity implements View.OnClickListener, AdapterView.OnItemClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final int DRAW_WHITEBOARD = 1;
    private final static long HANDLER_DELAY = 100;

    private ListView messagesView;
    private ImageButton sendBtn;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!timerInitialized) {
                chatStatusHandler.postDelayed(timerRunnable, 1000);
                return;
            }
            timeLeft = timeLeft > 0 ? timeLeft - 1 : 0;
            setTime(timeLeft);
            chatStatusHandler.postDelayed(timerRunnable, 1000);
        }
    };
    private RequestCallback timeLeftCallback = new RequestCallback() {
        @Override
        public void run(RequestResult result) {
            if (result.getStatus() != RequestResult.Status.OK) {
                Log.e("timeLeftC", "FAIL");
                return;
            }
            try {
                timeLeft = new JSONObject(result.getResponse()).getInt("time");
                if (timeLeft < 0) {
                    timeLeft = 0;
                }
                setTime(timeLeft);
                timerInitialized = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void setTime(int timeLeft) {
        String minutes = String.valueOf(timeLeft % 60);
        if (minutes.length() == 1) minutes = '0' + minutes;
        timerView.setText(String.valueOf(timeLeft / 60) + ":" + minutes);
    }

    private TextView timerView;


    private boolean initialized = false;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Color> playerColor = new HashMap<>();
    private int usedColors = 0;

    Color getPlayerColor(int playerId) {
        Color color = playerColor.get(playerId);
        if (color != null) return color;
        color = Color.values()[usedColors++];
        playerColor.put(playerId, color);
        Log.e("entry", ")))");
        for (Map.Entry<Integer, Color> entry : playerColor.entrySet()) {
            Log.e("entry", entry.getKey() + "-" + entry.getValue());
        }
        return color;
    }

    int getPlayerByColor(Color color) {
        for (Map.Entry<Integer, Color> entry : playerColor.entrySet()) {
            if (entry.getValue().equals(color)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException();
    }

    public enum Action {
        KICK, MUTE, CHOOSE;

        public int getItemId() {
            switch (this) {
                case KICK: return R.id.kick_item;
                case MUTE: return R.id.mute_item;
                case CHOOSE: return R.id.choose_item;
            }
            throw  new IllegalArgumentException();
        }

        static Action getAction(int id) {
            switch (id) {
                case R.id.kick_item: return KICK;
                case R.id.mute_item: return MUTE;
                case R.id.choose_item: return CHOOSE;
            }
            throw  new IllegalArgumentException();
        }
    }

    public enum Color {
        RED, PURPLE, YELLOW, GREEN;

        public int getTextViewId() {
            switch (this) {
                case RED: return R.drawable.textview_red;
                case PURPLE: return R.drawable.textview_purple;
                case YELLOW: return R.drawable.textview_yellow;
                case GREEN: return R.drawable.textview_green;
            }
            throw new IllegalArgumentException();
        }

        public int getPopupItemId() {
            switch (this) {
                case RED: return R.id.item_red;
                case PURPLE: return R.id.item_purple;
                case YELLOW: return R.id.item_yellow;
                case GREEN: return R.id.item_green;
            }
            throw new IllegalArgumentException();
        }

        static public Color getColorByItem(int id) {
            switch (id) {
                case R.id.item_red: return RED;
                case R.id.item_green: return GREEN;
                case R.id.item_purple: return PURPLE;
                case R.id.item_yellow: return YELLOW;
            }
            throw new IllegalArgumentException();
        }

        public int getColorId() {
            switch (this) {
                case RED: return R.color.red;
                case PURPLE: return R.color.purple;
                case YELLOW: return R.color.yellow;
                case GREEN: return R.color.light_green;
            }
            throw new IllegalArgumentException();
        }


    }

    final private ServiceConnection chatServiceConection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            chatService = ((ChatService.ChatBinder) service).getChatService();
            handler.postDelayed(getMessagesRunnable, HANDLER_DELAY);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            chatService = null;
        }
    };
    protected ChatService chatService = null;

    protected EditText messageInput;
    protected MessageAdapter messageAdapter;

    private ImageButton whiteboardBtn;
    private int alreadyRead = 0;
    private String whiteboardEncoded = "";

    final private Handler handler = new Handler();
    private boolean timerInitialized = false;
    private int timeLeft = 0;
    final private Runnable getMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            if (chatService != null) {
                final List<Message> messages = chatService.getMessages();
                for (Message message : messages.subList(alreadyRead, messages.size())) {
                    // Add our own messages only on initialization
                    if (!initialized || message.getAuthorId() != ProfileManager.getPlayer().getId()) {
                        messageAdapter.add(message);
                    }
                }
                alreadyRead = messages.size();
                if (!initialized && timerInitialized && chatService.initialized()) {
                    initialized = true;
                    completeInitialization();
                }
            }
            handler.postDelayed(this, HANDLER_DELAY);
        }
    };

    final private Handler chatStatusHandler = new Handler();
    final private Runnable chatStatusRunnable = new Runnable() {
        @Override
        public void run() {
            int id = ProfileManager.getPlayer().getId();
            if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.IDLE) {
                // for spectator let's use leader's id
                id = getIntent().getIntExtra("leader_id", -1);
                if (id == -1) {
                    throw new RuntimeException("Trying to spectate without leader_id provided");
                }

            }
            RequestMaker.chatStatus(id, ProfileManager.getPlayer().getChatId(), chatStatusCallback);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        createDrawer();
        messageInput = (EditText) findViewById(R.id.message_input);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setVisibility(View.GONE);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        messagesView.setAdapter(messageAdapter);
        messagesView.setOnItemClickListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        timerView = (TextView) headerLayout.findViewById(R.id.timer_view);

        sendBtn = (ImageButton) findViewById(R.id.send_button);
        whiteboardBtn = (ImageButton) findViewById(R.id.whiteboard_btn);
        sendBtn.setEnabled(false);

        sendBtn.setOnClickListener(this);
        whiteboardBtn.setOnClickListener(this);

        if (ProfileManager.getPlayer().getChatId() == -1) {
            throw new RuntimeException("Created ChatActivity without providing chat id.");
        }

        final Intent chatServiceIntent = new Intent(this, ChatService.class);
        bindService(chatServiceIntent, chatServiceConection, Context.BIND_AUTO_CREATE);

        chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
        RequestMaker.getTimeLeft(ProfileManager.getPlayer().getChatId(), timeLeftCallback);
        chatStatusHandler.postDelayed(timerRunnable, 1000); // run each second
    }

    private void completeInitialization() {
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
        spinner.setVisibility(View.GONE);
        messagesView.setVisibility(View.VISIBLE);
        sendBtn.setEnabled(true);
        if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.IDLE) {
            // if player is spectator hide input bar
            View inputBar = findViewById(R.id.input_bar);
            inputBar.setVisibility(View.GONE);
        }
    }


    public void postMessage() {
        final String messageText = messageInput.getText().toString();
        if (whiteboardEncoded.isEmpty() && messageText.trim().isEmpty()) {
            return;
        }
        messageInput.setText("");

        // Save whiteboard locally
        String whiteboardTag = "";
        if (!whiteboardEncoded.isEmpty()) {
            try {
                File localWhiteboard = File.createTempFile("my_", "", MyApplication.storageDir);
                whiteboardTag = localWhiteboard.getName();
                ChatService.saveWhiteboard(localWhiteboard, whiteboardEncoded);
            } catch (IOException e) {
                Log.d("postMessage", e.getMessage());
            }
        }

        // Post image to list view
        final Message message = new Message(-1, messageText, ProfileManager.getPlayer().getId(),
                ProfileManager.getPlayer().getChatId(), whiteboardTag);
        message.setStatus(Message.Status.SENDING);
        sendMessage(message, whiteboardEncoded);
        whiteboardEncoded = "";
        // Change to display that there's no whiteboard attached
        whiteboardBtn.setImageResource(R.drawable.whiteboard);

        messageAdapter.add(message);
    }

    public void sendMessage(final Message message, String whiteboard) {
        chatService.sendMessage(message.getText(), whiteboard, message.getTag(), new RequestCallback() {
            @Override
            public void run(RequestResult result) {
                if (result.getStatus() == RequestResult.Status.OK) {
                    Log.d("chS.sendM", "delivered");
                    message.setStatus(Message.Status.DELIVERED);
                } else {
                    Log.d("chS.sendM", "failed to deliver");
                    message.setStatus(Message.Status.FAILED);
                }
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy()", "called");
        stopService();
    }

    protected void stopService() {
        handler.removeCallbacks(getMessagesRunnable);
        chatStatusHandler.removeCallbacks(chatStatusRunnable);
        unbindService(chatServiceConection);
    }

    private boolean muted;
    final private RequestCallback chatStatusCallback = new RequestCallback() {
        @Override
        public void run(RequestResult requestResult) {
            if (requestResult.getStatus() != RequestResult.Status.OK) {
                chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
                return;
            }
            try {
                final JSONObject playerObject = new JSONObject(requestResult.getResponse());
                if (playerObject.has("error")) {
                    Log.d("ChatAct.iFHandler.run", playerObject.getString("error"));
                    return;
                }
                final String result = playerObject.getString("result");
                switch (result) {
                    case "running":
                        if (muted) {
                            muted = false;
                            Toast.makeText(ChatActivity.this, "Mute time ended, you can speak now.", Toast.LENGTH_LONG).show();
                            sendBtn.setEnabled(true);
                            messageInput.setEnabled(true);
                        }
                        chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
                        break;
                    case "kicked":
                        Toast.makeText(ChatActivity.this, "You were kicked from the chat",
                                Toast.LENGTH_LONG).show();
                        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);

                        finish();
                        break;
                    case "muted":
                        if (!muted) {
                            Toast.makeText(ChatActivity.this, "You were muted", Toast.LENGTH_LONG).show();
                            muted = true;
                            sendBtn.setEnabled(false);
                            messageInput.setEnabled(false);
                        }
                        chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
                        break;
                    default:
                        if (!result.equals("leaderboard_row")) {
                            final int newRating = playerObject.getInt("rating");
                            ProfileManager.getPlayer().setRating(newRating);
                            Toast.makeText(ChatActivity.this, result +
                                    "\nNew rating is " + newRating, Toast.LENGTH_LONG).show();
                        }
                        final Bundle bundle = new Bundle();
                        bundle.putSerializable("player_colors", (Serializable) playerColor);
                        bundle.putSerializable("player_ids", chatService.getPlayersId());
                        bundle.putInt("chat_id", ProfileManager.getPlayer().getChatId());
                        ProfileManager.getPlayer().setChatId(-1);
                        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);
                        final DialogFragment summaryFragment = new SummaryFragment();
                        summaryFragment.setArguments(bundle);
                        summaryFragment.show(getFragmentManager(), "");

                        messageInput.setEnabled(false);
                        sendBtn.setEnabled(false);
                        //finish();

                        break;
                }
            } catch (JSONException e) {
                Log.e("ChatAct.iFHandler.run", e.getMessage());
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_WHITEBOARD) {
            if (resultCode != RESULT_OK) {
                return;
            }

            final byte[] whiteboardBytes = data.getByteArrayExtra("whiteboard");

            whiteboardEncoded = Base64.encodeToString(whiteboardBytes, Base64.NO_WRAP);

            // Change it to display that whiteboard is attached
            whiteboardBtn.setImageResource(R.drawable.whiteboard_red);

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_button: {
                postMessage();
                break;
            }
            case R.id.whiteboard_btn: {
                final Intent intent = new Intent(this, WhiteboardActivity.class);
                startActivityForResult(intent, DRAW_WHITEBOARD);
                break;
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    private void navigate(int id) {
        switch (id) {
            case R.id.menu_change_password: {
                MenuActivity.ChangePasswordDialog changePasswordDialog = new MenuActivity.ChangePasswordDialog();
                changePasswordDialog.show(getFragmentManager(), "");
                break;
            }
            case R.id.menu_leaderboard: {
                final Intent intent = new Intent(this, LeaderboardActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menu_log_out: {
                finish();
                break;
            }
            default: {
                getItemButton(navigationView.getMenu(), Action.getAction(id)).performClick();
            }
        }
        createDrawer();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        navigate(menuItem.getItemId());
        menuItem.setChecked(false);
        return true;
    }


    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;

    void createDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        if (ProfileManager.getPlayerStatus() != ProfileManager.PlayerStatus.CHATTING_AS_LEADER) {
            menu.getItem(Action.KICK.ordinal()).setVisible(false);
            menu.getItem(Action.MUTE.ordinal()).setVisible(false);
            menu.getItem(Action.CHOOSE.ordinal()).setVisible(false);
        } else {
            for (Action action : Action.values())
                setupMenuItem(menu, action);
        }

        final View headerLayout = navigationView.getHeaderView(0);
        final TextView loginView = (TextView) headerLayout.findViewById(R.id.menu_header_login);
        final TextView ratingView = (TextView) headerLayout.findViewById(R.id.menu_header_rating);
        // TODO: FIX REMOVE CONSTANTS
        menu.getItem(5).setVisible(false); // disable spectate
        menu.getItem(6).setVisible(false); // disable_logout
        loginView.setText(ProfileManager.getPlayer().getLogin());
        ratingView.setText(String.valueOf(ProfileManager.getPlayer().getRating()));
    }

    void setupMenuItem(Menu menu, Action action) {
        Button locButton = getItemButton(menu, action);
        locButton.setVisibility(View.INVISIBLE);
        locButton.setOnClickListener(new MenuItemListener(action));
    }

    private Button getItemButton(Menu menu, Action action) {
        return (Button) MenuItemCompat.getActionView(menu.findItem(action.getItemId()));
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    private void kick(final int playerId) {
        RequestMaker.kick(playerId, ProfileManager.getPlayer().getChatId(), new RequestCallback() {
            @Override
            public void run(RequestResult result) {
                try {
                    final JSONObject playerObject = new JSONObject(result.getResponse());
                    if (playerObject.has("error")) {
                        Toast.makeText(ChatActivity.this, playerObject.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        chatService.getPlayersId().remove((Integer)playerId);
                        Toast.makeText(ChatActivity.this, "kicked", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e("kickCallback", e.getMessage());
                }
            }
        });
    }
    private static final int MUTE_TIME = 30;

    private void mute(int playerId) {
        RequestMaker.mute(playerId, ProfileManager.getPlayer().getChatId(), MUTE_TIME);
        //Toast.makeText(ChatActivity.this, "muted" + playerId, Toast.LENGTH_LONG).show();
    }
    private void choose(int playerId) {
        RequestMaker.chooseWinner(playerId, RequestCallback.DO_NOTHING);
        Toast.makeText(ChatActivity.this, "choose" + playerId, Toast.LENGTH_LONG).show();
    }

    private class MenuItemListener implements View.OnClickListener {
        private final Action action;
        private MenuItemListener(Action action) {
            this.action = action;
        }
        public void onClick(View v) {
            showPopup(v);
        }
        private void showPopup(View v) {
            Context wrapper = new ContextThemeWrapper(ChatActivity.this, R.style.popupMenuStyle);
            PopupMenu popup = new PopupMenu(wrapper, v);
            popup.inflate(R.menu.popup_menu);
            Collection<Color> colors = new ArrayList<>();
            Menu menu = popup.getMenu();
            for (int playerId : chatService.getPlayersId()) {
                colors.add(getPlayerColor(playerId));
            }
            for (Color color : Color.values()) {
                MenuItem menuItem = menu.findItem(color.getPopupItemId());
                if (!colors.contains(color)) {
                    menuItem.setVisible(false);
                } else {
                    //SpannableString s = new SpannableString(menuItem.getTitle());
                    final SpannableStringBuilder s = new SpannableStringBuilder(menuItem.getTitle());
                    final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
                    s.setSpan(bss, 0, s.length(), 0);
                    s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ChatActivity.this, color.getColorId())), 0, s.length(), 0);
                    menuItem.setTitle(s);
                }
            }
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int playerId = getPlayerByColor(Color.getColorByItem(item.getItemId()));
                    switch (action) {
                        case KICK: {
                            kick(playerId);
                            break;
                        }
                        case MUTE: {
                            mute(playerId);
                            break;
                        }
                        case CHOOSE: {
                            choose(playerId);
                            break;
                        }
                    }
                    return true;

                }
            });
            popup.show();
        }
    }
}