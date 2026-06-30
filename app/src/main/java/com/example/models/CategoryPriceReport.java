package com.example.models;

public class CategoryPriceReport {

    private final int categoryID;
    private final String categoryName;

    private final double averagePrice;
    private final long lowestPrice;
    private final long highestPrice;

    public CategoryPriceReport(
            int categoryID,
            String categoryName,
            double averagePrice,
            long lowestPrice,
            long highestPrice
    ) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.averagePrice = averagePrice;
        this.lowestPrice = lowestPrice;
        this.highestPrice = highestPrice;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public long getLowestPrice() {
        return lowestPrice;
    }

    public long getHighestPrice() {
        return highestPrice;
    }
}