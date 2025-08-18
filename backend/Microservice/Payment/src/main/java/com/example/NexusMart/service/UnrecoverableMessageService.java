package com.example.NexusMart.service;

import com.example.NexusMart.model.FailedMessage;
import com.example.NexusMart.repository.FailedMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnrecoverableMessageService {

    @Autowired
    private FailedMessageRepository failedMessageRepository;

    public void saveUnrecoverableMessage(String topic, String message, String error) {
        FailedMessage failedMessage = new FailedMessage();
        failedMessage.setTopic(topic);
        failedMessage.setMessage(message);
        failedMessage.setError(error);
        failedMessageRepository.save(failedMessage);
    }
}
