package com.example.restbreak;

import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import static com.example.restbreak.RestBreakApplication.getJobService;

public class AddFragment extends DialogFragment {

    public interface EventListUpdating {
        void EventListUpdatingString ();
    }

    private EventListUpdating EventListListener;
    private Button notification_info;
    private String date_type = "";
    private String event_change_type = "";
    private Button button_start, button_stop;
    private EditText description;
    private Calendar today;
    private Date date_start, date_stop;
    private int id = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        Button button_delete;

        try {
            EventListListener = (EventListUpdating) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        today = Calendar.getInstance();
        date_start = today.getTime();
        today.add(Calendar.MINUTE, 5);
        date_stop = today.getTime();

        button_start=view.findViewById(R.id.start);
        button_start.setText(String.format(getResources().getString(R.string.start), " \u270E"));
        button_start.setOnClickListener(view_ -> {
            date_type = "start";
            today.setTime(date_start);
            setTime();
        });

        button_stop=view.findViewById(R.id.stop);
        button_stop.setText(String.format(getResources().getString(R.string.stop), " \u270E"));
        button_stop.setOnClickListener(view_ -> {
            date_type = "stop";
            today.setTime(date_stop);
            setTime();
        });

        description = view.findViewById(R.id.description);

        Bundle args = getArguments();
        if (args != null) {
            if (args.keySet().contains("id")) {
                id = getArguments().getInt("id");
            }
            if (args.keySet().contains("date_start")) {
                date_start = DateConverter.toDate(getArguments().getString("date_start"));
                button_start.setText(String.format(getResources().getString(R.string.start),
                        DateConverter.fromTime(date_start)));
            }
            if (args.keySet().contains("date_stop")) {
                date_stop = DateConverter.toDate(getArguments().getString("date_stop"));
                button_stop.setText(String.format(getResources().getString(R.string.stop),
                        DateConverter.fromTime(date_stop)));
            }
            if (args.keySet().contains("description")) {
                description.setText(getArguments().getString("description"));
            }
            if (args.keySet().contains("type")) {
                event_change_type = getArguments().getString("type");
            }
        }

        button_delete=view.findViewById(R.id.delete);
        button_delete.setOnClickListener(view_ -> {
            event_change_type = "delete";
            String[] params = createEventParams();
            new NewEvent().execute(params);
            dismiss();
        });

        notification_info=view.findViewById(R.id.notification_info);
        notification_info.setOnClickListener(view_ -> {
            if (!notification_info.getText().toString().equals("")) {
                String[] params = createEventParams();
                new NewEvent().execute(params);
                dismiss();
                //Toast.makeText(getActivity(), "уведомление добавлено", Toast.LENGTH_SHORT)
                //        .show();
            }
        });

        if (!button_start.getText().toString()
                .equals(String.format(getResources().getString(R.string.start), " \u270E")) &
                !button_stop.getText().toString()
                        .equals(String.format(getResources().getString(R.string.stop),
                                " \u270E"))) {
            notification_info.setVisibility(View.VISIBLE);
            notification_info.setText(String.format(getResources()
                            .getString(R.string.save_notification),
                            (date_stop.getTime() - date_start.getTime()) / 1000 / 60));
        }
        if (event_change_type.equals("update")) {
            button_delete.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View form = getActivity().getLayoutInflater().inflate(R.layout.fragment_add, null);
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(form);
        dialog.getWindow().setGravity(Gravity.BOTTOM | Gravity.CENTER);
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(p);
        return dialog;
    }

    private void setTime() {
        new TimePickerDialog(getActivity(), calendarTime,
                today.get(Calendar.HOUR_OF_DAY),
                today.get(Calendar.MINUTE), true)
                .show();
    }

    TimePickerDialog.OnTimeSetListener calendarTime = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            today.set(Calendar.HOUR_OF_DAY, hourOfDay);
            today.set(Calendar.MINUTE, minute);
            if (date_type.equals("start")) {
                date_start = today.getTime();
                button_start.setText(String.format(getResources().getString(R.string.start),
                        DateConverter.fromTime(date_start)));
                if (button_stop.getText().toString()
                        .substring(getResources().getString(R.string.stop).length() - 2)
                        .compareTo(button_start.getText().toString()
                                .substring(getResources().getString(R.string.start).length() - 2))
                        <= 0) {
                    today.add(Calendar.MINUTE, 5);
                    date_stop = today.getTime();
                    button_stop.setText(String.format(getResources().getString(R.string.stop),
                            DateConverter.fromTime(date_stop)));

                }
            } else if (date_type.equals("stop")) {
                date_stop = today.getTime();
                button_stop.setText(String.format(getResources().getString(R.string.stop),
                        DateConverter.fromTime(date_stop)));
            }

            if (!button_start.getText().toString()
                    .equals(String.format(getResources().getString(R.string.start), " \u270E")) &
                    !button_stop.getText().toString()
                    .equals(String.format(getResources().getString(R.string.stop), " \u270E"))) {
                notification_info.setVisibility(View.VISIBLE);
                notification_info
                        .setText(String.format(getResources().getString(R.string.save_notification),
                                (date_stop.getTime() - date_start.getTime()) / 1000 / 60));
            }
        }
    };

    public String[] createEventParams() {
        String[] params = new String[6];
        params[0] = DateConverter.fromDate(date_start);
        params[1] = DateConverter.fromDate(date_stop);
        params[2] = description.getText().toString();
        params[3] = Integer.toString(id);
        params[4] = event_change_type;
        return params;
    }

    class NewEvent extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {
            Event event = new Event();
            event.start = DateConverter.toDate(params[0]);
            event.stop = DateConverter.toDate(params[1]);
            event.description = params[2];
            event.id = Integer.parseInt(params[3]);
            if (event.stop.getTime() - event.start.getTime() <= 0) {
                return null;
            }
            if (params[4].equals("create")) {
                event.id = RestBreakApplication.getDatabase().EventDao().getMaxId() + 1;
                RestBreakApplication.getDatabase().EventDao().insert(event);
                today = Calendar.getInstance();
                if (event.start.getTime() - today.getTime().getTime() > 0) {
                    scheduleJob(event.id, event.start.getTime() - today.getTime().getTime(),
                            params[4]);
                }
            } else if (params[4].equals("update")) {
                RestBreakApplication.getDatabase().EventDao().delete(event);
                RestBreakApplication.getDatabase().EventDao().insert(event);
            } else if (params[4].equals("delete")) {
                RestBreakApplication.getDatabase().EventDao().delete(event);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            EventListListener.EventListUpdatingString();
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
                    (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(exerciseJobBuilder.build());
        }
    }
}