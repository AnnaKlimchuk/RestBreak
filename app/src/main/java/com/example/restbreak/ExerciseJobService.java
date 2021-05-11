package com.example.restbreak;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.ExecutionException;

public class ExerciseJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        String CHANNEL_ID = "rest_notification";

        int id = params.getJobId();
        if (id == -1) {
            return false;
        }
        LoadDayTMP task = new LoadDayTMP();
        task.execute(id);
        Event event = new Event();
        try {
            event = task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), event.toString(), Toast.LENGTH_LONG).show();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Напоминание")
                        .setContentText("Пора покормить кота")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    static class LoadDayTMP extends AsyncTask<Integer, Void, Event> {
        @Override
        protected Event doInBackground(final Integer... params) {
            return RestBreakApplication.getDatabase().EventDao().getById(params[0]);
        }
    }
}
