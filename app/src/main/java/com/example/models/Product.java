package com.example.models;

public class Product {

    private int productID;
    private long tikiProductID;
    private int categoryID;

    private String productName;

    private long originalPrice;
    private long promotionalPrice;

    private String imageURL;
    private String productURL;
    private String description;

    private double rating;
    private String createdAt;

    public Product(
            int productID,
            long tikiProductID,
            int categoryID,
            String productName,
            long originalPrice,
            long promotionalPrice,
            String imageURL,
            String productURL,
            String description,
            double rating,
            String createdAt
    ) {
        this.productID = productID;
        this.tikiProductID = tikiProductID;
        this.categoryID = categoryID;
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.promotionalPrice = promotionalPrice;
        this.imageURL = imageURL;
        this.productURL = productURL;
        this.description = description;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public long getTikiProductID() {
        return tikiProductID;
    }

    public void setTikiProductID(long tikiProductID) {
        this.tikiProductID = tikiProductID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(long originalPrice) {
        this.originalPrice = originalPrice;
    }

    public long getPromotionalPrice() {
        return promotionalPrice;
    }

    public void setPromotionalPrice(long promotionalPrice) {
        this.promotionalPrice = promotionalPrice;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getProductURL() {
        return productURL;
    }

    public void setProductURL(String productURL) {
        this.productURL = productURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}