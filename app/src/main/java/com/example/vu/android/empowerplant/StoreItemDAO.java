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

    @Query("DELETE FROM storeitem")
    public void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StoreItem> storeItems);
    //This assumes that PKs of demo data are static. Dynamically generated skus/product ids could problematically accumulate entries across simulations.


}
