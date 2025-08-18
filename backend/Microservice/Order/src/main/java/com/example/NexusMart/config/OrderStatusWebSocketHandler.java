package com.example.NexusMart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class OrderStatusWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusWebSocketHandler.class);
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        logger.info("WebSocket connection established: " + sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("Received message: " + payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        logger.info("WebSocket connection closed: " + sessionId);
    }

    public void notifyOrderStatusChange(Long orderId, String status) {
        String jsonMessage = String.format("{\"orderId\": %d, \"status\": \"%s\"}", orderId, status);
        TextMessage message = new TextMessage(jsonMessage);
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    log.info("Sending message: " + message.getPayload());
                    session.sendMessage(message);
                } catch (IOException e) {
                    logger.error("Error sending WebSocket message", e);
                }
            } else {
                logger.warn("Session is closed: " + session.getId());
            }
        });
    }
}
