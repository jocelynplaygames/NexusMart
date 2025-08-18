package com.example.NexusMart.consumer;

import com.example.NexusMart.pojoClass.PaymentRequest;
import com.example.NexusMart.pojoClass.PaymentResult;
import com.example.NexusMart.service.PaymentService;
import com.example.NexusMart.service.UnrecoverableMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class PaymentConsumer {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UnrecoverableMessageService unrecoverableMessageService;

    @KafkaListener(topics = "payEvent", groupId = "payment-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        log.info("Received payment result message: {}", record.value());
        try {
            PaymentRequest paymentRequest = objectMapper.readValue(record.value(), PaymentRequest.class);
            log.info("Deserialized payment request: {}", paymentRequest);

            if (!isValidPaymentRequest(paymentRequest)) {
                log.error("Invalid payment request: {}", record.value());
                saveUnrecoverableMessage(record.topic(), record.value(), "Invalid payment request");
                acknowledgment.acknowledge();
                return;
            }

            boolean paymentSuccessful = paymentService.processPayment(paymentRequest.getTotalAmount());
            log.info("Processed payment for order ID: {} with result: {}", paymentRequest.getOrderId(), paymentSuccessful);

            PaymentResult paymentResult = new PaymentResult(paymentRequest.getOrderId(), paymentSuccessful, paymentRequest.getCartId(), paymentRequest.getToken());
            String paymentResultJson = objectMapper.writeValueAsString(paymentResult);
            log.info("Serialized payment result: {}", paymentResultJson);

            sendWithRetry("paymentResultTopic", paymentResultJson, acknowledgment);
            log.info("Payment result sent to Kafka for order ID: {}, success: {}", paymentRequest.getOrderId(), paymentSuccessful);
        } catch (JsonProcessingException e) {
            log.error("Error processing payment request or serializing payment result", e);
            saveUnrecoverableMessage(record.topic(), record.value(), e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    private boolean isValidPaymentRequest(PaymentRequest paymentRequest) {
        return paymentRequest.getOrderId() != null &&
                paymentRequest.getTotalAmount() > 0 &&
                paymentRequest.getCartId() != null &&
                paymentRequest.getToken() != null && !paymentRequest.getToken().isEmpty();
    }

    private void sendWithRetry(String topic, String message, Acknowledgment acknowledgment) {
        int retryCount = 0;
        int maxRetries = 5;
        while (retryCount < maxRetries) {
            try {
                log.info("Attempting to send message to topic: {}, attempt: {}", topic, retryCount + 1);
                kafkaTemplate.send(topic, message).get();
                acknowledgment.acknowledge();
                log.info("Message sent successfully to topic: {}", topic);
                return;
            } catch (InterruptedException | ExecutionException e) {
                retryCount++;
                log.error("Error sending payment result to Kafka, attempt: {}", retryCount, e);
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.error("Failed to send payment result to Kafka after {} attempts. Saving to MySQL for manual inspection.", maxRetries);
        saveUnrecoverableMessage(topic, message, "Max retry attempts reached");
    }

    private void saveUnrecoverableMessage(String topic, String message, String error) {
        log.info("Saving unrecoverable message to database. Topic: {}, Message: {}, Error: {}", topic, message, error);
        unrecoverableMessageService.saveUnrecoverableMessage(topic, message, error);
    }
}



