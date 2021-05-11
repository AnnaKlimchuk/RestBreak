package com.example.restbreak;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ShowFragment extends ListFragment {

    public interface EventUpdating {
        void EventUpdatingString (Integer id, Date date_start, Date date_stop, String description);
    }

    private EventUpdating EventListener;
    private List<Event> event_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show, null);

        ListAdapter adapter;
        String[] dates  = new String[2];
        Calendar today = Calendar.getInstance();

        try {
            EventListener = (EventUpdating) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        dates[0] = DateConverter.fromDate(today.getTime());

        today.add(Calendar.DAY_OF_YEAR, 1);
        dates[1] = DateConverter.fromDate(today.getTime());

        Plan.LoadDay task = new Plan.LoadDay();
        task.execute(dates);
        event_list = new ArrayList<>();
        try {
            event_list = task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        String[] event_string_list = new String[event_list.size()];
        for (int i=0; i<event_list.size(); i++) {
            event_string_list[i] = event_list.get(i).toString();
        }
        if (getActivity() != null) {
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                    event_string_list);
            setListAdapter(adapter);
        } else {
            throw new RuntimeException("null returned from getActivity()");
        }
        return view;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Event event = event_list.get(position);
        EventListener.EventUpdatingString(event.id, event.start, event.stop, event.description);
    }
}