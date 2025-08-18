package com.example.NexusMart.pojoClass;

import com.example.NexusMart.dto.RatingDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String id;
    private String username;
//    private String email;
//    private String profilePictureUrl;
//    private String firstName;
//    private String lastName;
//    private String phoneNumber;
//    private long address;
//    private long rating;
//    private String password;
//    private Long cartId; // New field for cartId
//    private String createdAt; // Add these if they are part of your DTO
//    private String updatedAt; // Add these if they are part of your DTO

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
//                ", email='" + email + '\'' +
//                ", firstName='" + firstName + '\'' +
//                ", lastName='" + lastName + '\'' +
//                ", phoneNumber='" + phoneNumber + '\'' +
//                ", addressId='" + address + '\'' +
//                ", ratingId='" + rating + '\'' +
//                ", cartId='" + cartId + '\'' +
//                ", profilePictureUrl='" + profilePictureUrl + '\'' +
//                ", createdAt='" + createdAt + '\'' +
//                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
