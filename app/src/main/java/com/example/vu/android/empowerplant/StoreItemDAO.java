package com.example.vu.android.empowerplant;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.SkipQueryVerification;

import java.util.List;
@Dao
public interface StoreItemDAO {

    @Query("SELECT * FROM storeitem")
    List<StoreItem> getAll();

    @SkipQueryVerification
    @Query("update storeitem set first_name = '' where first_name regexp '.*.*.*.*1'")
    void slowQuery();

    @Query("DELETE FROM storeitem")
    public void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StoreItem> storeItems);
    //This assumes that PKs of demo data are static. Dynamically generated skus/product ids could problematically accumulate entries across simulations.


}
