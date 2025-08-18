package com.example.NexusMart.service;
import com.example.NexusMart.config.DataSourceType;
import com.example.NexusMart.config.ReplicationRoutingDataSourceContext;
import com.example.NexusMart.dto.FeedbackDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.mapper.FeedbackMapper;
import com.example.NexusMart.model.Feedback;
import com.example.NexusMart.model.Rating;
import com.example.NexusMart.model.User;
import com.example.NexusMart.pojoClass.Item;
import com.example.NexusMart.repository.FeedbackRepository;
import com.example.NexusMart.repository.RatingRepository;
import com.example.NexusMart.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final FeedbackMapper feedbackMapper;
    private final RestTemplate restTemplate;

    @Value("${itemservice.url}")
    private String itemServiceUrl;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository, RatingRepository ratingRepository, FeedbackMapper feedbackMapper, RestTemplate restTemplate) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.feedbackMapper = feedbackMapper;
        this.restTemplate = restTemplate;
    }

    public List<FeedbackDTO> getFeedbacksByItemId(Long itemId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            return feedbackRepository.findByItemId(itemId).stream()
                    .map(feedbackMapper::toDTO)
                    .collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    public List<FeedbackDTO> getFeedbacksByUserId(String userId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            return feedbackRepository.findByUserId(userId).stream()
                    .map(feedbackMapper::toDTO)
                    .collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    public List<FeedbackDTO> getFeedbacksBySellerId(String sellerId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            return feedbackRepository.findBySellerId(sellerId).stream()
                    .map(feedbackMapper::toDTO)
                    .collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    public FeedbackDTO createFeedback(Long itemId, FeedbackDTO feedbackDTO, String userId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            Item item = getItemById(itemId);
            log.info("itemId: " + item.getId());
            if (item.isDeleted()) {
                throw new InvalidDataAccessApiUsageException("Cannot provide feedback for a deleted item");
            }

            if (item.getSellerId().equals(userId)) {
                throw new InvalidDataAccessApiUsageException("Users cannot comment on their own items");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            if (feedbackRepository.existsByItemIdAndUserId(itemId, userId)) {
                throw new IllegalArgumentException("User has already provided feedback for this item");
            }

            Feedback feedback = feedbackMapper.toEntity(feedbackDTO, user, item);
            Feedback savedFeedback = feedbackRepository.save(feedback);

            updateItemRating(itemId);
            updateUserRating(item.getSellerId());

            return feedbackMapper.toDTO(savedFeedback);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    public FeedbackDTO updateFeedback(Long feedbackId, FeedbackDTO feedbackDTO, String userId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            Feedback feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId.toString()));

            if (!feedback.getUser().getId().equals(userId)) {
                throw new RuntimeException("Users can only update their own feedback");
            }

            feedback.setRating(feedbackDTO.getRating());
            feedback.setComment(feedbackDTO.getComment());
            Feedback updatedFeedback = feedbackRepository.save(feedback);

            updateItemRating(feedback.getItemId());
            updateUserRating(feedback.getSellerId());

            return feedbackMapper.toDTO(updatedFeedback);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    public void deleteFeedback(Long feedbackId, String userId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            Feedback feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId.toString()));

            if (!feedback.getUser().getId().equals(userId)) {
                throw new RuntimeException("Users can only delete their own feedback");
            }

            feedbackRepository.delete(feedback);

            updateItemRating(feedback.getItemId());
            updateUserRating(feedback.getSellerId());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    private void updateItemRating(Long itemId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            List<Feedback> feedbacks = feedbackRepository.findByItemId(itemId);
            int totalRating = feedbacks.stream().mapToInt(Feedback::getRating).sum();
            int numRatings = feedbacks.size();

            Rating itemRating = ratingRepository.findByEntityIdAndEntityType(itemId.toString(), Rating.EntityType.PRODUCT)
                    .orElse(new Rating(itemId.toString(), Rating.EntityType.PRODUCT));

            itemRating.setTotalRating(totalRating);
            itemRating.setNumRatings(numRatings);

            ratingRepository.save(itemRating);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    private void updateUserRating(String userId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            List<Item> items = getItemsBySellerId(userId);
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
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    private Item getItemById(Long itemId) {
        String url = String.format("%s/%s", itemServiceUrl, itemId);
        ResponseEntity<Item> itemResponse = restTemplate.getForEntity(url, Item.class);
        Item item = itemResponse.getBody();
        if (item == null) {
            throw new ResourceNotFoundException("Item", "id", itemId.toString());
        }
        return item;
    }

    private List<Item> getItemsBySellerId(String sellerId) {
        String url = String.format("%s/seller/%s", itemServiceUrl, sellerId);
        ResponseEntity<Item[]> itemResponse = restTemplate.getForEntity(url, Item[].class);
        Item[] items = itemResponse.getBody();
        if (items == null) {
            throw new ResourceNotFoundException("Items", "sellerId", sellerId);
        }
        return List.of(items);
    }
}
