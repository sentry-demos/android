package com.example.vu.android.empowerplant;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {StoreItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract StoreItemDAO StoreItemDAO();

    private static volatile AppDatabase INSTANCE;
    //singleton recommended
    public static synchronized AppDatabase getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), 
            AppDatabase.class,"AppDatabase")
            .allowMainThreadQueries()
            .build();
        }
        return INSTANCE;
    }
}
