package ru.spbau.shevchenko.chatbattle.frontend;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import java.util.Arrays;
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
    private static final long HANDLER_DELAY = 100;
    private static final int MUTE_TIME = 30;

    private ListView messagesView;
    private ImageButton sendBtn;
    private TextView timerView;
    private boolean destroyed = false;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Color> playerColor = new HashMap<>();
    private int usedColors = 0;

    private boolean initialized = false;

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

    final private Handler handler = new Handler();
    private ImageButton whiteboardBtn;
    private int alreadyRead = 0;
    private String whiteboardEncoded = "";

    private boolean timerInitialized = false;
    private int timeLeft = 0;
    private int chatId;
    private boolean spectating;

    final private Handler chatStatusHandler = new Handler();
    final private Runnable chatStatusRunnable = new Runnable() {
        @Override
        public void run() {
            int id = ProfileManager.getPlayer().getId();
            if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.IDLE) {
                // for spectator let's use leader's id
                id = getIntent().getIntExtra("leader_id", -1);
                if (id == -1) {
                    throw new RuntimeException("Trying to drawer_spectate without leader_id provided");
                }

            }
            RequestMaker.chatStatus(id, chatId, chatStatusCallback);
        }
    };
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
            if (destroyed) {
                return;
            }
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

    public enum Color {

        RED, PURPLE, YELLOW, GREEN;

        public int getTextViewId() {
            switch (this) {
                case RED:
                    return R.drawable.textview_red;
                case PURPLE:
                    return R.drawable.textview_purple;
                case YELLOW:
                    return R.drawable.textview_yellow;
                case GREEN:
                    return R.drawable.textview_green;
            }
            throw new IllegalArgumentException();
        }

        public int getPopupItemId() {
            switch (this) {
                case RED:
                    return R.id.item_red;
                case PURPLE:
                    return R.id.item_purple;
                case YELLOW:
                    return R.id.item_yellow;
                case GREEN:
                    return R.id.item_green;
            }
            throw new IllegalArgumentException();
        }


        static public Color getColorByItem(int id) {
            switch (id) {
                case R.id.item_red:
                    return RED;
                case R.id.item_green:
                    return GREEN;
                case R.id.item_purple:
                    return PURPLE;
                case R.id.item_yellow:
                    return YELLOW;
            }
            throw new IllegalArgumentException();
        }

        public int getColorId() {
            switch (this) {
                case RED:
                    return R.color.red500;
                case PURPLE:
                    return R.color.purple;
                case YELLOW:
                    return R.color.yellow;
                case GREEN:
                    return R.color.light_green;
            }
            throw new IllegalArgumentException();
        }

    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        createDrawer();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
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

        spectating = getIntent().hasExtra("leader_id");
        if (spectating) {
            chatId = getIntent().getIntExtra("chat_id", -1);
        }
        else {
            chatId = ProfileManager.getPlayer().getChatId();
        }

        if (spectating && ProfileManager.getPlayer().getChatId() != chatId) {
            // if player is spectator hide input bar
            View inputBar = findViewById(R.id.input_bar);
            inputBar.setVisibility(View.GONE);
        }

        final Intent chatServiceIntent = new Intent(this, ChatService.class);
        chatServiceIntent.putExtra("chat_id", chatId);
        bindService(chatServiceIntent, chatServiceConection, Context.BIND_AUTO_CREATE);

        chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
        RequestMaker.getTimeLeft(chatId, timeLeftCallback);
        chatStatusHandler.postDelayed(timerRunnable, 1000); // run each second
        Log.e("chat", "created");
    }

    private void completeInitialization() {
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.initializing_progress_bar);
        spinner.setVisibility(View.GONE);
        messagesView.setVisibility(View.VISIBLE);
        sendBtn.setEnabled(true);
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
                whiteboardTag);
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
        destroyed = true;
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
            if (destroyed) {
                return;
            }
            if (!isVisible) {
                chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
                return;
            }
            Log.d("chatStatus", "enter");
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
                            muted = true;
                            Toast.makeText(ChatActivity.this, "You were muted", Toast.LENGTH_LONG).show();
                            sendBtn.setEnabled(false);
                            messageInput.setEnabled(false);
                        }
                        chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
                        break;
                    default:
                        if (!result.equals("leader")) {
                            final int newRating = playerObject.getInt("rating");
                            ProfileManager.getPlayer().setRating(newRating);
                            Toast.makeText(ChatActivity.this, result +
                                    "\nNew rating is " + newRating, Toast.LENGTH_LONG).show();
                        }
                        final Bundle bundle = new Bundle();
                        bundle.putSerializable("player_colors", (Serializable) playerColor);
                        bundle.putSerializable("player_ids", chatService.getPlayersId());
                        bundle.putInt("chat_id", chatId);
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

        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
                if (!whiteboardEncoded.isEmpty()) {
                    intent.putExtra("whiteboard", whiteboardEncoded);
                }
                startActivityForResult(intent, DRAW_WHITEBOARD);
                break;
            }
        }
    }

    private void setTime(int timeLeft) {
        String minutes = String.valueOf(timeLeft % 60);
        if (minutes.length() == 1) minutes = '0' + minutes;
        timerView.setText(String.valueOf(timeLeft / 60) + ":" + minutes);
    }


    public Color getPlayerColor(int playerId) {
        Color color = playerColor.get(playerId);
        if (color != null) return color;
        color = Color.values()[usedColors++];
        playerColor.put(playerId, color);
        return color;
    }

    private int getPlayerByColor(Color color) {
        for (Map.Entry<Integer, Color> entry : playerColor.entrySet()) {
            if (entry.getValue().equals(color)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }


    @Override
    protected void createDrawer() {
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        if (ProfileManager.getPlayerStatus() != ProfileManager.PlayerStatus.CHATTING_AS_LEADER
                || (spectating && ProfileManager.getPlayer().getChatId() != chatId)) {
            navigationView.getMenu().setGroupVisible(R.id.chat_actions_group, false);
        } else {
            for (DrawerAction.LeaderAction action : DrawerAction.LeaderAction.values())
                setupMenuItem(menu, action);
        }

        final View headerLayout = navigationView.getHeaderView(0);
        final TextView loginView = (TextView) headerLayout.findViewById(R.id.menu_header_login);
        final TextView ratingView = (TextView) headerLayout.findViewById(R.id.menu_header_rating);
        menu.getItem(DrawerAction.SwitchActivityAction.SPECTATE.getPosition()).setVisible(false);
        loginView.setText(ProfileManager.getPlayer().getLogin());
        ratingView.setText(String.valueOf(ProfileManager.getPlayer().getRating()));
    }

    void setupMenuItem(Menu menu, DrawerAction.LeaderAction action) {
        Button locButton = getItemButton(menu, action);
        locButton.setVisibility(View.INVISIBLE);
        locButton.setOnClickListener(new MenuItemListener(action));
    }

    private Button getItemButton(Menu menu, DrawerAction.LeaderAction action) {
        return (Button) MenuItemCompat.getActionView(menu.findItem(action.getItemId()));
    }


    @Override
    public void onBackPressed() {
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    @Override
    protected boolean specialCheck(int id) {
        if (DrawerAction.SwitchActivityAction.LEADERBOARD.getItemId() == id) {
            final Intent intent = new Intent(this, LeaderboardActivity.class);
            startActivityForResult(intent, NO_MATTER_CODE);
            return true;
        }
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        final Menu menu = navigationView.getMenu();
        for (DrawerAction.LeaderAction action : DrawerAction.LeaderAction.values()) {
            if (action.getItemId() == id) {
                getItemButton(menu,action).performClick();
                return true;
            }

        }
        return false;
    }

    private void kick(final int playerId) {
        RequestMaker.kick(playerId, chatId, new RequestCallback() {
            @Override
            public void run(RequestResult result) {
                try {
                    final JSONObject playerObject = new JSONObject(result.getResponse());
                    if (playerObject.has("error")) {
                        Toast.makeText(ChatActivity.this, playerObject.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        chatService.getPlayersId().remove((Integer) playerId);
                        Toast.makeText(ChatActivity.this, "kicked", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e("kickCallback", e.getMessage());
                }
            }
        });
    }

    private void mute(int playerId) {
        RequestMaker.mute(playerId, chatId, MUTE_TIME);
        //Toast.makeText(ChatActivity.this, "muted" + playerId, Toast.LENGTH_LONG).show();
    }

    private void choose(int playerId) {
        RequestMaker.chooseWinner(playerId, RequestCallback.DO_NOTHING);
    }

    private class MenuItemListener implements View.OnClickListener {
        private final DrawerAction.LeaderAction action;

        private MenuItemListener(DrawerAction.LeaderAction action) {
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