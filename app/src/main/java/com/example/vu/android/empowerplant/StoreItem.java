package com.example.vu.android.empowerplant;

import androidx.fragment.app.Fragment;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity
public class StoreItem  {//why does this extend Fragment?
    @NotNull
    @PrimaryKey
    String sku;
    @ColumnInfo(name = "first_name")
    String name;
    @ColumnInfo(name = "image")
    String image;
    @ColumnInfo(name = "type")
    String type;
    @ColumnInfo(name = "id")
    int id;
    @ColumnInfo(name = "price")
    int price;
    @ColumnInfo(name = "quantity")
    int quantity;
    @Ignore
    public StoreItem() {
    }

    public StoreItem(String sku, String name, String image, String type, int id, int price, int quantity) {
        this.sku = sku;
        this.name = name;
        this.image = image;
        this.type = type;
        this.id = id;
        this.price = price;
        this.quantity = quantity;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setItemId(int id) {
        this.id = id;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setQuantity(int quantity){ this.quantity = quantity;}

    public int getQuantity() {
        return quantity;
    }
    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getType() {
        return type;
    }

    public int getItemId() {
        return id;
    }

    public int getPrice() {
        return price;
    }
}


