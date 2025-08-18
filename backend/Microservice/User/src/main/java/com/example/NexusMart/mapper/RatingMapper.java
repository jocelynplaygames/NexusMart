package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.RatingDTO;
import com.example.NexusMart.model.Rating;

public class RatingMapper {

    public static RatingDTO toDTO(Rating rating) {
        if (rating == null) {
            return null;
        }

        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setId(rating.getId());
        ratingDTO.setEntityId(rating.getEntityId());
        ratingDTO.setEntityType(rating.getEntityType().name());
        ratingDTO.setTotalRating(rating.getTotalRating());
        ratingDTO.setNumRatings(rating.getNumRatings());

        return ratingDTO;
    }

    public static Rating toEntity(RatingDTO ratingDTO) {
        if (ratingDTO == null) {
            return null;
        }

        Rating rating = new Rating();
        rating.setId(ratingDTO.getId());
        rating.setEntityId(ratingDTO.getEntityId());
        rating.setEntityType(Rating.EntityType.valueOf(ratingDTO.getEntityType()));
        rating.setTotalRating(ratingDTO.getTotalRating());
        rating.setNumRatings(ratingDTO.getNumRatings());

        return rating;
    }
}

