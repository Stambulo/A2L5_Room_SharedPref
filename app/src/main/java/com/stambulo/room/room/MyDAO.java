package com.stambulo.room.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MyDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addWeatherHistory(WeatherHistory weatherHistory);

    @Query("select * from weather_history")
    public List<WeatherHistory> getWeatherHistory();
}
