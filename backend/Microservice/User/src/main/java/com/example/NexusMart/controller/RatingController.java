package com.example.NexusMart.controller;

import com.example.NexusMart.dto.RatingDTO;
import com.example.NexusMart.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RatingDTO> getRatingById(@PathVariable Long id) {
        RatingDTO ratingDTO = ratingService.getRatingById(id);
        return ResponseEntity.ok(ratingDTO);
    }

    @GetMapping("/entity/{entityId}/{entityType}")
    public ResponseEntity<RatingDTO> getRatingByEntityIdAndType(@PathVariable String entityId, @PathVariable String entityType) {
        RatingDTO ratingDTO = ratingService.getRatingByEntityIdAndType(entityId, entityType);
        return ResponseEntity.ok(ratingDTO);
    }

    @PostMapping
    public ResponseEntity<RatingDTO> createRating(@RequestBody RatingDTO ratingDTO) {
        RatingDTO createdRating = ratingService.createRating(ratingDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRating);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RatingDTO> updateRating(@PathVariable Long id, @RequestBody RatingDTO ratingDTO) {
        RatingDTO updatedRating = ratingService.updateRating(id, ratingDTO);
        return ResponseEntity.ok(updatedRating);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }
}

