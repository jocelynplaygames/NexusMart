package com.example.NexusMart.pojoClass;

public class PaymentRequest {
    private Long orderId;
    private double totalAmount;
    private Long cartId;
    private String token;

    
    public PaymentRequest(Long orderId, double totalAmount, Long cartId, String token) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.cartId = cartId;
        this.token = token;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


