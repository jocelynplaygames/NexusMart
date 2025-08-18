// File: backend/NexusMart/src/main/java/com/example/NexusMart/service/FeedbackService.java
package com.example.NexusMart.service;
import com.example.NexusMart.dto.FeedbackDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.mapper.FeedbackMapper;
import com.example.NexusMart.model.Feedback;
import com.example.NexusMart.model.Item;
import com.example.NexusMart.model.Rating;
import com.example.NexusMart.model.User;
import com.example.NexusMart.repository.FeedbackRepository;
import com.example.NexusMart.repository.ItemRepository;
import com.example.NexusMart.repository.RatingRepository;
import com.example.NexusMart.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final FeedbackMapper feedbackMapper;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, ItemRepository itemRepository, UserRepository userRepository, RatingRepository ratingRepository, FeedbackMapper feedbackMapper) {
        this.feedbackRepository = feedbackRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.feedbackMapper = feedbackMapper;
    }

    public List<FeedbackDTO> getFeedbacksByItemId(Long itemId) {
        return feedbackRepository.findByItemId(itemId).stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbacksByUserId(String userId) {
        return feedbackRepository.findByUserId(userId).stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbacksBySellerId(String sellerId) {
        return feedbackRepository.findBySellerId(sellerId).stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
    }

    public FeedbackDTO createFeedback(Long itemId, FeedbackDTO feedbackDTO, String userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId.toString()));

        if (item.isDeleted()) {
            throw new InvalidDataAccessApiUsageException("Cannot provide feedback for a deleted item");
        }

        if (item.getSeller().getId().equals(userId)) {
            throw new InvalidDataAccessApiUsageException("Users cannot comment on their own items");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (feedbackRepository.existsByItemIdAndUserId(itemId, userId)) {
            throw new IllegalArgumentException("User has already provided feedback for this item");
        }

        Feedback feedback = new Feedback();
        feedback.setItem(item);
        feedback.setUser(user);
        feedback.setRating(feedbackDTO.getRating());
        feedback.setComment(feedbackDTO.getComment());
        Feedback savedFeedback = feedbackRepository.save(feedback);

        updateItemRating(itemId);
        updateUserRating(item.getSeller().getId());

        return feedbackMapper.toDTO(savedFeedback);
    }

    public FeedbackDTO updateFeedback(Long feedbackId, FeedbackDTO feedbackDTO, String userId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId.toString()));

        if (!feedback.getUser().getId().equals(userId)) {
            throw new RuntimeException("Users can only update their own feedback");
        }

        feedback.setRating(feedbackDTO.getRating());
        feedback.setComment(feedbackDTO.getComment());
        Feedback updatedFeedback = feedbackRepository.save(feedback);

        updateItemRating(feedback.getItem().getId());
        updateUserRating(feedback.getItem().getSeller().getId());

        return feedbackMapper.toDTO(updatedFeedback);
    }

    public void deleteFeedback(Long feedbackId, String userId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId.toString()));

        if (!feedback.getUser().getId().equals(userId)) {
            throw new RuntimeException("Users can only delete their own feedback");
        }

        feedbackRepository.delete(feedback);

        updateItemRating(feedback.getItem().getId());
        updateUserRating(feedback.getItem().getSeller().getId());
    }

    private void updateItemRating(Long itemId) {
        List<Feedback> feedbacks = feedbackRepository.findByItemId(itemId);
        int totalRating = feedbacks.stream().mapToInt(Feedback::getRating).sum();
        int numRatings = feedbacks.size();

        Rating itemRating = ratingRepository.findByEntityIdAndEntityType(itemId.toString(), Rating.EntityType.PRODUCT)
                .orElse(new Rating(itemId.toString(), Rating.EntityType.PRODUCT));

        itemRating.setTotalRating(totalRating);
        itemRating.setNumRatings(numRatings);

        ratingRepository.save(itemRating);
    }

    private void updateUserRating(String userId) {
        List<Item> items = itemRepository.findBySellerId(userId);
        int totalRating = 0;
        int numRatings = 0;

        for (Item item : items) {
            List<Feedback> feedbacks = feedbackRepository.findByItemId(item.getId());
            totalRating += feedbacks.stream().mapToInt(Feedback::getRating).sum();
            numRatings += feedbacks.size();
        }

        Rating userRating = ratingRepository.findByEntityIdAndEntityType(userId, Rating.EntityType.SELLER)
                .orElse(new Rating(userId, Rating.EntityType.SELLER));

        userRating.setTotalRating(totalRating);
        userRating.setNumRatings(numRatings);

        ratingRepository.save(userRating);
    }
}

