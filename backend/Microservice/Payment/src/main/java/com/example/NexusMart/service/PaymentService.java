// File: backend/Microservice/Payment/src/main/java/com/example/NexusMart/service/PaymentService.java
package com.example.NexusMart.service;


import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    public boolean processPayment(double totalAmount) {
        return true;
    }
}