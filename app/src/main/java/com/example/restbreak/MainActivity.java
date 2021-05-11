package com.example.restbreak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button plan, profile;

        setTimeVal();

        plan = findViewById(R.id.plan);
        plan.setOnClickListener(view -> {
            Intent startActivity = new Intent(MainActivity.this, Plan.class);
            startActivity(startActivity);
        });

        profile = findViewById(R.id.profile);
        profile.setOnClickListener(view -> {
            Intent startActivity = new Intent(MainActivity.this, Profile.class);
            startActivity(startActivity);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTimeVal();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTimeVal();
    }

    static class NearDate extends AsyncTask<Date, Void, Date> {
        @Override
        protected Date doInBackground(final Date ... param) {
            if (RestBreakApplication.getDatabase().EventDao().loadDay(param[0], param[1]).size()
                    != 0) {
                return RestBreakApplication.getDatabase().EventDao().
                        getNearDate(param[0]);
            } else {
                return param[0];
            }
        }
    }

    private void setTimeVal() {
        TextView time, time_dist;
        NearDate task = new NearDate();
        Calendar today = Calendar.getInstance();
        Date[] dates = new Date[2];
        Date near_date;

        dates[0] = today.getTime();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.add(Calendar.DAY_OF_YEAR, 1);
        dates[1] = today.getTime();
        task.execute(dates);
        near_date = dates[0];
        try {
            near_date = task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        time = findViewById(R.id.time);
        time_dist = findViewById(R.id.time_dist);
        if (near_date.getTime() - dates[0].getTime() > 0) {
            time.setText(DateConverter.fromTime(near_date));
            time_dist.setText(String.format(Locale.getDefault(), "%d",(near_date.getTime() - dates[0].getTime())
                    / 60 / 1000));
        } else {
            time.setText(getResources().getString(R.string.not_set));
            time_dist.setText(getResources().getString(R.string.not_set));
        }
    }
}