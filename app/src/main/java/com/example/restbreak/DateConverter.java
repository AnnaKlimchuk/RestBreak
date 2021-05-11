package com.example.restbreak;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {
    public static DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
    public static DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

    public static String fromTime(Date date) {
        return timeFormat.format(date);
    }

    @TypeConverter
    public static String fromDate(Date date) {
        return df.format(date);
    }

    @TypeConverter
    public static Date toDate(String date_str) {
        try {
            return df.parse(date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(System.currentTimeMillis());
    }
}
