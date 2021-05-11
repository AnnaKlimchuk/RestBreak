package com.example.restbreak;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.restbreak.RestBreakApplication.APP_PREFERENCES_TIME_START;
import static com.example.restbreak.RestBreakApplication.APP_PREFERENCES_TIME_STOP;
import static com.example.restbreak.RestBreakApplication.getJobService;

public class Plan extends FragmentActivity implements AddFragment.EventListUpdating,
        ShowFragment.EventUpdating {

    private TextView add_default, delete_all;
    private FragmentManager manager;
    private Calendar today;
    private List<Event> event_list;
    private String[] dates;
    private LoadDay task;

    @Override
    public void EventListUpdatingString() {
        manager.findFragmentById(R.id.fragment_show).onCreateView(LayoutInflater.from(this),
                null,null);

        task = new LoadDay();
        task.execute(dates);
        event_list = new ArrayList<>();
        try {
            event_list = task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (event_list.size() == 0) {
            add_default.setVisibility(View.VISIBLE);
            delete_all.setVisibility(View.GONE);
        } else {
            add_default.setVisibility(View.GONE);
            delete_all.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void EventUpdatingString(Integer id, Date date_start, Date date_stop,
                                    String description) {
        AddFragment addFragment = new AddFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putString("date_start", DateConverter.fromDate(date_start));
        args.putString("date_stop", DateConverter.fromDate(date_stop));
        args.putString("description", description);
        args.putString("type", "update");
        addFragment.setArguments(args);
        addFragment.show(manager, "myDialog");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        TextView add;

        manager = getSupportFragmentManager();

        add = findViewById(R.id.add);
        add.setOnClickListener(view -> {
            AddFragment addFragment = new AddFragment();
            Bundle args = new Bundle();
            args.putString("type", "create");
            addFragment.setArguments(args);
            addFragment.show(manager, "myDialog");
        });

        dates = new String[2];
        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        dates[0] = DateConverter.fromDate(today.getTime());
        today.add(Calendar.DAY_OF_YEAR, 1);
        dates[1] = DateConverter.fromDate(today.getTime());

        task = new LoadDay();
        task.execute(dates);
        event_list = new ArrayList<>();
        try {
            event_list = task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        add_default = findViewById(R.id.add_default);
        delete_all = findViewById(R.id.delete_all);
        if (event_list.size() == 0) {
            add_default.setVisibility(View.VISIBLE);
            delete_all.setVisibility(View.GONE);
        }
        add_default.setOnClickListener(view -> {
            String[] params = new String[5];
            params[0] = "";
            params[1] = "";
            params[2] = "";
            params[3] = "";
            params[4] = "create";
            String date_start_string = RestBreakApplication.getPreferences()
                    .getString(APP_PREFERENCES_TIME_START, "");
            String date_stop_string = RestBreakApplication.getPreferences()
                    .getString(APP_PREFERENCES_TIME_STOP, "");
            today = Calendar.getInstance();
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            for (int i=Integer.parseInt(date_start_string.substring(0, 2)) + 1;
                i<Integer.parseInt(date_stop_string.substring(0, 2)); i++) {
                today.set(Calendar.HOUR_OF_DAY, i);
                params[0] += DateConverter.fromDate(today.getTime()) + ";";
                today.add(Calendar.MINUTE, 5);
                params[1] += DateConverter.fromDate(today.getTime()) + ";";
                today.set(Calendar.MINUTE, 0);
                params[3] += "-1;";
            }
            new NewEvents().execute(params);
        });

        delete_all.setOnClickListener(view -> {
            String[] params = new String[5];
            params[0] = "";
            params[1] = "";
            params[2] = "";
            params[3] = "";
            params[4] = "delete";
            for (int i=0;i<event_list.size();i++) {
                params[0] += dates[0] + ";";
                params[1] += dates[1] + ";";
                params[3] += event_list.get(i).id + ";";
            }
            new NewEvents().execute(params);
        });
    }

    static class LoadDay extends AsyncTask<String, Void, List<Event>> {
        @Override
        protected List<Event> doInBackground(final String... params) {
            Date date_start = DateConverter.toDate(params[0]);
            Date date_stop = DateConverter.toDate(params[1]);
            return RestBreakApplication.getDatabase().EventDao().loadDay(date_start, date_stop);
        }
    }

    class NewEvents extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {
            Event event = new Event();
            event.description = params[2];
            String[] start_splits = params[0].trim().split(";");
            String[] stop_splits = params[1].trim().split(";");
            String[] id_splits = params[3].trim().split(";");
            for (int i = 0; i < id_splits.length;i++) {
                event.start = DateConverter.toDate(start_splits[i]);
                event.stop = DateConverter.toDate(stop_splits[i]);
                event.id = Integer.parseInt(id_splits[i]);
                if ((event.stop.getTime() - event.start.getTime() <= 0) &
                        (!params[4].equals("delete"))) {
                    continue;
                }
                if (params[4].equals("create")) {
                    event.id = RestBreakApplication.getDatabase().EventDao().getMaxId() + 1;
                    RestBreakApplication.getDatabase().EventDao().insert(event);
                    today = Calendar.getInstance();
                    if (event.start.getTime() - today.getTime().getTime() > 0) {
                        scheduleJob(event.id, event.start.getTime() -
                                today.getTime().getTime(), params[4]);
                    }
                } else if (params[4].equals("update")) {
                    RestBreakApplication.getDatabase().EventDao().delete(event);
                    RestBreakApplication.getDatabase().EventDao().insert(event);
                } else if (params[4].equals("delete")) {
                    RestBreakApplication.getDatabase().EventDao().delete(event);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            EventListUpdatingString();
        }
    }

    private void scheduleJob(Integer id, long delay, String event_type) {
        if (event_type.equals("create")) {
            JobInfo.Builder exerciseJobBuilder = new JobInfo.Builder(id, getJobService());
            exerciseJobBuilder.setMinimumLatency(0);
            exerciseJobBuilder.setOverrideDeadline(10*1000);
            exerciseJobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            exerciseJobBuilder.setRequiresDeviceIdle(false);
            exerciseJobBuilder.setRequiresCharging(false);
            exerciseJobBuilder.setBackoffCriteria(10*1000,
                    JobInfo.BACKOFF_POLICY_LINEAR);

            JobScheduler jobScheduler =
                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(exerciseJobBuilder.build());
        }
    }
}