// File: backend/Microservice/Order/src/main/java/com/example/NexusMart/model/Order.java
package com.example.NexusMart.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
@NamedEntityGraph(
        name = "Order.items",
        attributeNodes = @NamedAttributeNode("orderItems")
)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "shipping_method_id", nullable = false)
    private ShippingMethod shippingMethod;

    @Column(name = "card_type", nullable = false)
    private String cardType;

    @Column(name = "last_four_digit", nullable = false)
    private String lastFourDigit;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}

