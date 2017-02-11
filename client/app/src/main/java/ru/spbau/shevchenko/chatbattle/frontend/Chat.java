package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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


public class Chat extends BasicActivity implements View.OnClickListener, AdapterView.OnItemClickListener,
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
            RequestMaker.chatStatus(ProfileManager.getPlayer().getId(),
                    ProfileManager.getPlayer().getChatId(), chatStatusCallback);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        createDrawer();
        messageInput = (EditText) findViewById(R.id.message_input);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setVisibility(View.GONE);
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());
        messagesView.setAdapter(messageAdapter);
        messagesView.setOnItemClickListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        View headerLayout = navigationView.getHeaderView(0);
        timerView = (TextView) headerLayout.findViewById(R.id.timer_view);

        sendBtn = (ImageButton) findViewById(R.id.send_button);
        whiteboardBtn = (ImageButton) findViewById(R.id.whiteboard_btn);
        sendBtn.setEnabled(false);

        sendBtn.setOnClickListener(this);
        whiteboardBtn.setOnClickListener(this);

        if (ProfileManager.getPlayer().getChatId() == -1) {
            throw new RuntimeException("Created Chat without providing chat id.");
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
    }


    public void postMessage() {
        final String messageText = messageInput.getText().toString();
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
                Log.d("ChatAct.iFHandler.run", requestResult.getResponse());
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
                            Toast.makeText(Chat.this, "Mute time ended, you can speak now.", Toast.LENGTH_LONG).show();
                            sendBtn.setEnabled(true);
                            messageInput.setEnabled(true);
                        }
                        chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
                        break;
                    case "kicked":
                        Toast.makeText(Chat.this, "You were kicked from the chat",
                                Toast.LENGTH_LONG).show();
                        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);

                        finish();
                        break;
                    case "muted":
                        if (!muted) {
                            Toast.makeText(Chat.this, "You were muted", Toast.LENGTH_LONG).show();
                            muted = true;
                            sendBtn.setEnabled(false);
                            messageInput.setEnabled(false);
                        }
                        chatStatusHandler.postDelayed(chatStatusRunnable, HANDLER_DELAY);
                        break;
                    default:
                        if (!result.equals("leader")) {
                            final int newRating = playerObject.getInt("rating");
                            ProfileManager.getPlayer().setRating(newRating);
                            Toast.makeText(Chat.this, result +
                                    "\nNew rating is " + newRating, Toast.LENGTH_LONG).show();
                        }
                        ProfileManager.getPlayer().setChatId(-1);
                        ProfileManager.setPlayerStatus(ProfileManager.PlayerStatus.IDLE);

                        finish();
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
            whiteboardBtn.setImageResource(R.mipmap.whiteboard_drawn);
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


    private DrawerLayout mDrawerLayout;

    void createDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.CHATTING_AS_PLAYER) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);
        } else {
            for (Action action : Action.values())
                setupMenuItem(menu, action);
        }
    }

    void setupMenuItem(Menu menu, Action action) {
        ImageButton locButton = (ImageButton) menu.findItem(action.getItemId()).getActionView();
        locButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.brush));
        locButton.setOnClickListener(new MenuItemListener(action));
    }


    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // update highlighted item in the navigation menu
        menuItem.setChecked(true);
//        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
                        Toast.makeText(Chat.this, playerObject.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        chatService.getPlayersId().remove((Integer)playerId);
                        Toast.makeText(Chat.this, "kicked", Toast.LENGTH_LONG).show();
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
        //Toast.makeText(Chat.this, "muted" + playerId, Toast.LENGTH_LONG).show();
    }
    private void choose(int playerId) {
        RequestMaker.chooseWinner(playerId, RequestCallback.DO_NOTHING);
        Toast.makeText(Chat.this, "choose" + playerId, Toast.LENGTH_LONG).show();
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
            PopupMenu popup = new PopupMenu(Chat.this, v);
            popup.inflate(R.menu.popup_menu);
            Collection<Color> colors = new ArrayList<>();
            Menu menu = popup.getMenu();
            for (int playerId : chatService.getPlayersId()) {
                colors.add(getPlayerColor(playerId));
            }
            for (Color color : Color.values()) {
                if (!colors.contains(color)) {
                    MenuItem menuItem = menu.findItem(color.getPopupItemId());
                    menuItem.setVisible(false);
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