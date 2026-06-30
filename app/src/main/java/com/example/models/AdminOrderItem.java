package com.example.models;

public class AdminOrderItem {

    private final long orderID;
    private final int userID;

    private final String customerName;
    private final String customerEmail;

    private final String orderDate;
    private final long totalAmount;

    private final String shippingAddress;
    private final String paymentMethod;
    private final String paymentStatus;

    public AdminOrderItem(
            long orderID,
            int userID,
            String customerName,
            String customerEmail,
            String orderDate,
            long totalAmount,
            String shippingAddress,
            String paymentMethod,
            String paymentStatus
    ) {
        this.orderID = orderID;
        this.userID = userID;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public long getOrderID() {
        return orderID;
    }

    public int getUserID() {
        return userID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}