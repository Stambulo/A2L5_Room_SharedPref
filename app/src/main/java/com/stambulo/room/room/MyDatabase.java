package com.stambulo.room.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {WeatherHistory.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {

    public abstract MyDAO myDAO();
}
