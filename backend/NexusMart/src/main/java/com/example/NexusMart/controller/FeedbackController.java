// File: backend/NexusMart/src/main/java/com/example/NexusMart/controller/FeedbackController.java
package com.example.NexusMart.controller;

import com.example.NexusMart.dto.FeedbackDTO;
import com.example.NexusMart.jwt.JwtTokenProvider;
import com.example.NexusMart.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public FeedbackController(FeedbackService feedbackService, JwtTokenProvider jwtTokenProvider) {
        this.feedbackService = feedbackService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/item/{itemId}")
    public List<FeedbackDTO> getFeedbacksByItem(@PathVariable Long itemId) {
        return feedbackService.getFeedbacksByItemId(itemId);
    }

    @GetMapping("/user/{userId}")
    public List<FeedbackDTO> getFeedbacksByUser(@PathVariable String userId) {
        return feedbackService.getFeedbacksByUserId(userId);
    }

    @GetMapping("/seller/{sellerId}")
    public List<FeedbackDTO> getFeedbacksBySeller(@PathVariable String sellerId) {
        return feedbackService.getFeedbacksBySellerId(sellerId);
    }

    @PostMapping("/item/{itemId}")
    public ResponseEntity<FeedbackDTO> postFeedback(@PathVariable Long itemId, @RequestBody FeedbackDTO feedbackDTO, @RequestHeader("Authorization") String token) {
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        FeedbackDTO createdFeedback = feedbackService.createFeedback(itemId, feedbackDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFeedback);
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackDTO> updateFeedback(@PathVariable Long feedbackId, @RequestBody FeedbackDTO feedbackDTO, @RequestHeader("Authorization") String token) {
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        FeedbackDTO updatedFeedback = feedbackService.updateFeedback(feedbackId, feedbackDTO, userId);
        return ResponseEntity.ok(updatedFeedback);
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long feedbackId, @RequestHeader("Authorization") String token) {
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        feedbackService.deleteFeedback(feedbackId, userId);
        return ResponseEntity.noContent().build();
    }
}
