package com.example.restbreak;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.Locale;

@Entity
public class Event {
    @PrimaryKey
    public int id;
    @TypeConverters({DateConverter.class})
    public Date start;
    @TypeConverters({DateConverter.class})
    public Date stop;
    public String description;

    public Event(){
        this.id = -1;
        this.start = new Date(System.currentTimeMillis());
        this.stop = new Date(System.currentTimeMillis() + 5*60*1000);
        this.description = "";
    }

    /*public String toStringFull() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }*/

    @NonNull
    @Override
    public String toString() {
        String description = this.description;
        if (description.equals("")) {
            description = "отдых";
        }
        if (description.length() > 23) {
            description = description.substring(0, 23) + "... ";
        }
        return String.format(Locale.getDefault(),"%s %d минут %s",
                DateConverter.fromTime(this.start),
                (this.stop.getTime() - this.start.getTime()) / 60 / 1000,
                description);
    }
}
