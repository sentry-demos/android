package com.example.vu.android.empowerplant.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StoreItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun StoreItemDAO(): StoreItemDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // singleton recommended
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room
                            .databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java, "db"
                            )
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}