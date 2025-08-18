package com.example.NexusMart.pojoClass;

public class PaymentResult {
    private Long orderId;
    private boolean paymentSuccessful;
    private Long cartId;
    private String token;

    
    public PaymentResult(Long orderId, boolean paymentSuccessful, Long cartId, String token) {
        this.orderId = orderId;
        this.paymentSuccessful = paymentSuccessful;
        this.cartId = cartId;
        this.token = token;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    public void setPaymentSuccessful(boolean paymentSuccessful) {
        this.paymentSuccessful = paymentSuccessful;
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


