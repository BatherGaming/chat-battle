package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.MyApplication;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;
import ru.spbau.shevchenko.chatbattle.backend.RequestMaker;
import ru.spbau.shevchenko.chatbattle.backend.SearchHandler;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PREFS_FILE_NAME = "MyPrefsFile";
    private boolean triedAutoLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchHandler.getInstance().setMyApplication((MyApplication) getApplication());

        setContentView(R.layout.activity_login);

        if (!autoLogin()) showLayout(View.GONE, View.VISIBLE);

        final Button signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(this);
        final Button signUpButton = (Button) findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(this);
    }

    public void completeLogin() {
        ((EditText) findViewById(R.id.login_edit)).setText("");
        ((EditText) findViewById(R.id.password_edit)).setText("");
        ((TextView) findViewById(R.id.status_view)).setText("");


        final Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
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
        showLayout(View.GONE, View.VISIBLE);
    }


}
