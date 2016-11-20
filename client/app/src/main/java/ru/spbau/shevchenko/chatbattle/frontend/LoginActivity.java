package ru.spbau.shevchenko.chatbattle.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.spbau.shevchenko.chatbattle.R;
import ru.spbau.shevchenko.chatbattle.backend.ProfileManager;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signinButton = (Button) findViewById(R.id.signin_button);
        signinButton.setOnClickListener(this);
        Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this);
    }

    public void completeLogin(){
        ((EditText) findViewById(R.id.login_edit)).setText("");
        ((EditText) findViewById(R.id.password_edit)).setText("");
        ((TextView) findViewById(R.id.status_view)).setText("");


        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    public void failedLogin(String reason){
        TextView statusView = (TextView) findViewById(R.id.status_view);
        statusView.setText(reason);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signin_button: {
                TextView statusView = (TextView) findViewById(R.id.status_view);
                statusView.setText(R.string.signin_in);
                String login = ((EditText) findViewById(R.id.login_edit)).getText().toString().trim();
                String password = ((EditText) findViewById(R.id.password_edit)).getText().toString().trim();
                ProfileManager.signin(login, password, this);
                break;
            }
            case R.id.signup_button: {
                Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
