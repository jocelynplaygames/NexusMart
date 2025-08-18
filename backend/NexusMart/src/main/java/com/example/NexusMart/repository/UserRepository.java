// File: backend/NexusMart/src/main/java/com/example/NexusMart/repository/UserRepository.java
package com.example.NexusMart.repository;
import com.example.NexusMart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}
