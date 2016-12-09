package ru.spbau.shevchenko.chatbattle.backend;

import android.app.Application;
import android.util.Log;

import java.io.File;


public class MyApplication extends Application {
    public static File storageDir;

    public void onCreate() {
        super.onCreate();
        storageDir = getApplicationContext().getFilesDir();

        Log.d("onCreate", storageDir.getAbsolutePath());
    }
}