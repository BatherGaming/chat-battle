package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.SearcherRunnable;
import ru.spbau.shevchenko.chatbattle.backend.StringConstants;
import ru.spbau.shevchenko.chatbattle.backend.RequestResult;

public class LoginActivity extends BasicActivity implements View.OnClickListener {

    private static final String PREFS_FILE_NAME = "MyPrefsFile";
    static private boolean triedAutoLogin = true;
    static private Thread SearcherThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        showLayout(View.VISIBLE, View.GONE);

        if (triedAutoLogin && !autoLogin()) showLayout(View.GONE, View.VISIBLE);

        final Button signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(this);
        final Button signUpButton = (Button) findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(this);

        SearcherThread = new Thread(new SearcherRunnable((MyApplication) getApplication()));
        SearcherThread.start();

        initStringConstants();
    }

    public void completeLogin() {
        ((EditText) findViewById(R.id.login_edit)).setText("");
        ((EditText) findViewById(R.id.password_edit)).setText("");
        ((TextView) findViewById(R.id.status_view)).setText("");


        final Intent intent = new Intent(this, MenuActivity.class);
        startActivityForResult(intent, 1);
    }

    public void failedLogin(String reason) {
        if (triedAutoLogin) {
            triedAutoLogin = false;
            showLayout(View.GONE, View.VISIBLE);
        } else {
            final TextView statusView = (TextView) findViewById(R.id.status_view);
            statusView.setText(reason);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin_button: {
                final TextView statusView = (TextView) findViewById(R.id.status_view);
                statusView.setText(R.string.signin_in);
                final String login = ((EditText) findViewById(R.id.login_edit)).getText().toString().trim();
                final String password = ((EditText) findViewById(R.id.password_edit)).getText().toString().trim();
                ProfileManager.signIn(login, password, this);
                save(login, password);
                break;
            }
            case R.id.signup_button: {
                final Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                break;
            }
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

        spinner.setVisibility(spinnerVisibility);

        signInButton.setVisibility(layoutVisibility);
        signUpButton.setVisibility(layoutVisibility);
        loginEdit.setVisibility(layoutVisibility);
        passwordEdit.setVisibility(layoutVisibility);
        statusView.setVisibility(layoutVisibility);
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

    @Override
    public void onResume() {
        super.onResume();
        if (!triedAutoLogin) showLayout(View.GONE, View.VISIBLE);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
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

    void initStringConstants() {
        StringConstants.setHELLO(getString(R.string.hello));
        StringConstants.setPROFILE(getString(R.string.profile));
        StringConstants.setLOG_OUT(getString(R.string.log_out));
        StringConstants.setMALE(getString(R.string.male));
        StringConstants.setFEMALE(getString(R.string.female));

    }

    public void loginResponse(RequestResult.Status status) {
        switch (status) {
            case OK: {
                completeLogin();
            }
            case FAILED_CONNECTION: {
                failedLogin(getResources().getString(R.string.internet_troubles));
            }
            case ERROR: {
                failedLogin(getResources().getString(R.string.unknown_error));
            }
        }
    }
}
