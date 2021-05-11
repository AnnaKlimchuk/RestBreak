package com.example.restbreak;

import android.app.Application;
import android.content.ComponentName;
import android.content.SharedPreferences;

import androidx.room.Room;

public class RestBreakApplication extends Application {
    private static EventDatabase database;
    private static SharedPreferences profile_values;
    private static ComponentName jobService;
    public static final String APP_PREFERENCES = "profile_values";
    public static final String APP_PREFERENCES_TIME_START = "TIME_START";
    public static final String APP_PREFERENCES_TIME_STOP = "IME_STOP";
    public static EventDatabase getDatabase() {
        return database;
    }
    public static SharedPreferences getPreferences() {
        return profile_values;
    }
    public static ComponentName getJobService() {
        return jobService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(this, EventDatabase.class, "database")
                .build();
        profile_values = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        jobService = new ComponentName(this, ExerciseJobService.class);
        SharedPreferences.Editor editor = profile_values.edit();
        editor.putString(APP_PREFERENCES_TIME_START, "10:00");
        editor.putString(APP_PREFERENCES_TIME_STOP, "19:00");
        editor.apply();
    }
}
