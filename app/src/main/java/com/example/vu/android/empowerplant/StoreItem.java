package com.example.vu.android.empowerplant;

import androidx.fragment.app.Fragment;

public class StoreItem extends Fragment {


    String sku, name, image,type;
    int id, price, quantity;

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
