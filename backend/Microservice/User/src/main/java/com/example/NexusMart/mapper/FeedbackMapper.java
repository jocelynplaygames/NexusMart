package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.FeedbackDTO;
import com.example.NexusMart.model.Feedback;
import com.example.NexusMart.model.User;
import com.example.NexusMart.pojoClass.Item;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public FeedbackDTO toDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setItemId(feedback.getItemId());
        dto.setUserId(feedback.getUser().getId());
        dto.setUserName(feedback.getUser().getUsername());
        dto.setRating(feedback.getRating());
        dto.setComment(feedback.getComment());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());
        return dto;
    }

    public Feedback toEntity(FeedbackDTO dto, User user, Item item) {
        Feedback feedback = new Feedback();
        feedback.setItemId(item.getId());
        feedback.setSellerId(item.getSellerId());
        feedback.setUser(user);
        feedback.setRating(dto.getRating());
        feedback.setComment(dto.getComment());
        return feedback;
    }
}

