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

    @Query("select distinct storeitem.* from storeitem\n" +
           "inner join storeitem as s\n" +
           "inner join storeitem as s2\n" +
           "inner join storeitem as s3\n" +
           "inner join storeitem as s4\n" +
           "inner join storeitem as s5\n" +
           "inner join storeitem as s6")
    List<StoreItem> getAllSlow();

    @Query("DELETE FROM storeitem")
    public void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StoreItem> storeItems);
    //This assumes that PKs of demo data are static. Dynamically generated skus/product ids could problematically accumulate entries across simulations.


}
