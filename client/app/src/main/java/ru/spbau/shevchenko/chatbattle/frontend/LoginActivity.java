package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestCallback;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.SearcherRunnable;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class LoginActivity extends BasicActivity implements View.OnClickListener {

    private static final String PREFS_FILE_NAME = "MyPrefsFile";
    private static boolean triedAutoLogin = true;
    private static Thread SearcherThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final TextView chatTextView = (TextView) findViewById(R.id.chat_text_view);
        chatTextView.setTypeface(Typeface.createFromAsset(getAssets(),
                getResources().getString(R.string.KeyCapsFont)));

        final TextView battleTextView = (TextView) findViewById(R.id.battle_text_view);
        battleTextView.setTypeface(Typeface.createFromAsset(getAssets(),
                getResources().getString(R.string.KeyCapsFont)));

        showLayout(View.VISIBLE, View.GONE);

        if (triedAutoLogin && !autoLogin()) showLayout(View.GONE, View.VISIBLE);

        final Button signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(this);

        final Button signUpButton = (Button) findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(this);

        final Button resetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        resetPasswordButton.setOnClickListener(this);

        SearcherThread = new Thread(new SearcherRunnable((MyApplication) getApplication()));
        SearcherThread.start();
    }

    public void completeLogin() {
        ((TextView) findViewById(R.id.login_edit)).setText("");
        ((TextView) findViewById(R.id.password_edit)).setText("");
        ((TextView) findViewById(R.id.status_view)).setText("");

        final Intent intent = new Intent(this, MenuActivity.class);
        startActivityForResult(intent, 1);
    }

    public void failedLogin(CharSequence reason) {
        if (triedAutoLogin) {
            triedAutoLogin = false;
            showLayout(View.GONE, View.VISIBLE);
        } else {
            final TextView statusView = (TextView) findViewById(R.id.status_view);
            statusView.setText(reason);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!triedAutoLogin) showLayout(View.GONE, View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin_button: {
                final TextView statusView = (TextView) findViewById(R.id.status_view);
                statusView.setText(R.string.signin_in);
                final String login = ((TextView) findViewById(R.id.login_edit)).getText().toString().trim();
                final String password = ((TextView) findViewById(R.id.password_edit)).getText().toString().trim();
                ProfileManager.signIn(login, password, this);
                save(login, password);
                break;
            }
            case R.id.signup_button: {
                final Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.reset_password_button: {
                final String login = ((TextView) findViewById(R.id.login_edit)).getText().toString().trim();
                if (login.equals("")) {
                    final TextView statusView = (TextView) findViewById(R.id.status_view);
                    statusView.setText(getString(R.string.invalid_login));
                    return;
                }
                RequestMaker.reset_password(login, new RequestCallback() {
                    @Override
                    public void run(RequestResult result) {
                        try {
                            final TextView statusView = (TextView) findViewById(R.id.status_view);
                            final JSONObject playerObject = new JSONObject(result.getResponse());
                            if (playerObject.has("error")) {
                                statusView.setText(getString(R.string.invalid_login));
                            } else {
                                statusView.setText(getString(R.string.success_reset));
                            }
                        } catch (JSONException e) {
                            Log.e("resetPasswordCallback", e.getMessage());
                        }
                    }
                });
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.IN_QUEUE_AS_LEADER ||
                ProfileManager.getPlayerStatus() == ProfileManager.PlayerStatus.IN_QUEUE_AS_PLAYER) {
            RequestMaker.deleteFromQueue(ProfileManager.getPlayer().getId());
        }
        triedAutoLogin = false;
        if (data != null && data.getBooleanExtra("exit", false)) {
            SearcherThread.interrupt();
            finish();
        }
    }

    private void save(String login, String password) {
        final SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putString("login", login);
        editor.putString("password", password);
        editor.apply();
    }

    private void showLayout(int spinnerVisibility, int layoutVisibility) {
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.login_initializing_progress_bar);
        final Button signInButton = (Button) findViewById(R.id.signin_button);
        final Button signUpButton = (Button) findViewById(R.id.signup_button);
        final EditText loginEdit = (EditText) findViewById(R.id.login_edit);
        final EditText passwordEdit = (EditText) findViewById(R.id.password_edit);
        final TextView statusView = (TextView) findViewById(R.id.status_view);
        final Button reset = (Button) findViewById(R.id.reset_password_button);

        spinner.setVisibility(spinnerVisibility);

        signInButton.setVisibility(layoutVisibility);
        signUpButton.setVisibility(layoutVisibility);
        loginEdit.setVisibility(layoutVisibility);
        passwordEdit.setVisibility(layoutVisibility);
        statusView.setVisibility(layoutVisibility);
        reset.setVisibility(layoutVisibility);
    }

    private boolean autoLogin() {
        final SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
        final String login = settings.getString("login", "");
        final String password = settings.getString("password", "");

        if (!login.equals("")) {
            showLayout(View.VISIBLE, View.GONE);
            ProfileManager.signIn(login, password, this);
            return true;
        }
        return false;
    }

}
