package com.example.vu.android.empowerplant;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {StoreItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract StoreItemDAO StoreItemDAO();

    private static volatile AppDatabase INSTANCE;
    //singleton recommended
    static AppDatabase getInstance(Context context){
        if(INSTANCE == null){
            synchronized (AppDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,"AppDatabase").build();
                }
            }
        }
        return INSTANCE;
    }
}
