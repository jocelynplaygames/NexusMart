package com.example.NexusMart.pojoClass;

import com.example.NexusMart.dto.RatingDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String id;
    private String username;












    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +










                '}';
    }
}
