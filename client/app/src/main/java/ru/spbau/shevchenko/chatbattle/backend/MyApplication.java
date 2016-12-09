package ru.spbau.shevchenko.chatbattle.backend;

import android.app.Application;

import ru.spbau.shevchenko.chatbattle.frontend.BasicActivity;

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
    }

    private BasicActivity mCurrentActivity = null;

    public BasicActivity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(BasicActivity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }
}

