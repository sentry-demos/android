package com.example.vu.android.empowerplant.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class StoreItem(
    @PrimaryKey val sku: String,
    @ColumnInfo(name = "first_name") val name: String?,
    @ColumnInfo(name = "image") val image: String?,
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name = "id") val itemId: Int,
    @ColumnInfo(name = "price") val price: Int
)