package com.example.NexusMart.pojoClass;

public class OrderCreationMessage {
    private Long orderId;
    private Long cartId;
    private Long shippingMethodId;
    private Long cardId;
    private String token;

    
    public OrderCreationMessage(Long orderId, Long cartId, Long shippingMethodId, Long cardId, String token) {
        this.orderId = orderId;
        this.cartId = cartId;
        this.shippingMethodId = shippingMethodId;
        this.cardId = cardId;
        this.token = token;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getShippingMethodId() {
        return shippingMethodId;
    }

    public void setShippingMethodId(Long shippingMethodId) {
        this.shippingMethodId = shippingMethodId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
