package ru.spbau.shevchenko.chatbattle.backend;

import android.app.Application;

import java.io.File;
import ru.spbau.shevchenko.chatbattle.frontend.BasicActivity;

public class MyApplication extends Application {
    public static File storageDir;
    public void onCreate() {
        super.onCreate();
        storageDir = getApplicationContext().getFilesDir();
    }

    private BasicActivity mCurrentActivity = null;

    public BasicActivity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(BasicActivity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }
}



