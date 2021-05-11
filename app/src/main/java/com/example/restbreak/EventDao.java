package com.example.restbreak;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.util.List;
import java.util.Date;


@Dao
public interface EventDao {
    @Insert
    void insert(Event event);

    @Delete
    void delete(Event event);

    @TypeConverters({DateConverter.class})
    @Query("SELECT * FROM event WHERE stop >= :start and start <= :stop order by start")
    List<Event> loadDay(Date start, Date stop);

    @Query("SELECT * FROM event WHERE id = :id")
    Event getById(int id);

    @Query("SELECT max(id) FROM event")
    int getMaxId();

    @TypeConverters({DateConverter.class})
    @Query("SELECT min(start) FROM event WHERE start > :cur_date")
    Date getNearDate(Date cur_date);
}
