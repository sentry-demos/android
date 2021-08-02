package com.example.vu.android.toolstore;

public class StoreItem {

    String sku, name, image,type;
    int id, price;

    public StoreItem() {
    }

    public StoreItem(String sku, String name, String image, String type, int id, int price) {
        this.sku = sku;
        this.name = name;
        this.image = image;
        this.type = type;
        this.id = id;
        this.price = price;
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

    public void setId(int id) {
        this.id = id;
    }

    public void setPrice(int price) {
        this.price = price;
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

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }
}
