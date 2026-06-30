package com.example.models;

public class PotentialProductReport {

    private final int productID;
    private final String productName;

    private final long oldPrice;
    private final long newPrice;
    private final long amountSaved;

    private final double rating;

    public PotentialProductReport(
            int productID,
            String productName,
            long oldPrice,
            long newPrice,
            long amountSaved,
            double rating
    ) {
        this.productID = productID;
        this.productName = productName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.amountSaved = amountSaved;
        this.rating = rating;
    }

    public int getProductID() {
        return productID;
    }

    public String getProductName() {
        return productName;
    }

    public long getOldPrice() {
        return oldPrice;
    }

    public long getNewPrice() {
        return newPrice;
    }

    public long getAmountSaved() {
        return amountSaved;
    }

    public double getRating() {
        return rating;
    }
}