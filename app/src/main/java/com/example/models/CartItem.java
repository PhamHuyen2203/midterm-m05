package com.example.models;

public class CartItem {

    private final int cartID;
    private final int userID;
    private final int productID;

    private final String productName;
    private final String imageURL;

    private final long unitPrice;
    private final int quantity;

    public CartItem(
            int cartID,
            int userID,
            int productID,
            String productName,
            String imageURL,
            long unitPrice,
            int quantity
    ) {
        this.cartID = cartID;
        this.userID = userID;
        this.productID = productID;
        this.productName = productName;
        this.imageURL = imageURL;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public int getCartID() {
        return cartID;
    }

    public int getUserID() {
        return userID;
    }

    public int getProductID() {
        return productID;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getSubtotal() {
        return unitPrice * quantity;
    }
}