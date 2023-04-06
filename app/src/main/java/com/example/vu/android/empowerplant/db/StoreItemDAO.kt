package com.example.vu.android.empowerplant.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StoreItemDAO {
    @get:Query("SELECT * FROM storeitem")
    val all: List<StoreItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(storeItems: List<StoreItem>) //This assumes that PKs of demo data are static. Dynamically generated skus/product ids could problematically accumulate entries across simulations.
}