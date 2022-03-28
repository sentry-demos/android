package com.example.vu.android.empowerplant;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
@Dao
public interface StoreItemDAO {

    @Query("SELECT * FROM storeitem")
    List<StoreItem> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StoreItem> storeItems);


}
