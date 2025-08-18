package com.example.NexusMart.service;


import com.example.NexusMart.config.DataSourceType;                    
import com.example.NexusMart.config.ReplicationRoutingDataSourceContext; 
import com.example.NexusMart.dto.UserDTO;                             
import com.example.NexusMart.dto.CardDTO;                             
import com.example.NexusMart.mapper.CardMapper;                       
import com.example.NexusMart.exception.ResourceNotFoundException;     
import com.example.NexusMart.exception.UserAlreadyExistsException;    
import com.example.NexusMart.mapper.UserMapper;                       
import com.example.NexusMart.model.*;                                 
import com.example.NexusMart.repository.AddressRepository;           
import com.example.NexusMart.repository.CardRepository;              
import com.example.NexusMart.repository.RatingRepository;            
import com.example.NexusMart.repository.UserRepository;              
import com.example.NexusMart.jwt.JwtTokenProvider;                   
import jakarta.persistence.OptimisticLockException;                  
import lombok.extern.slf4j.Slf4j;                                    
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;             
import org.springframework.stereotype.Service;                        
import java.util.*;
import java.util.stream.Collectors;                                   

import org.springframework.transaction.annotation.Transactional;      
import org.springframework.web.multipart.MultipartFile;               
import java.io.IOException;
import java.util.Optional;

/**
 * User Business Service Layer
 * Responsible for handling user-related business logic, including user management, payment card management, file upload, etc.
 * 
 * Important functions:
 * 1. User CRUD operations (create, query, update, delete)
 * 2. Payment card management (add, delete, modify, query)
 * 3. Avatar file upload to cloud storage
 * 4. Keycloak identity authentication integration
 * 5. Database master-slave replication routing
 * 6. Optimistic lock concurrency control
 * 
 * Technical features:
 * - Use master-slave database separation for read/write operations
 * - Integrate Keycloak for identity authentication
 * - Use Cloudinary for file storage
 * - Implement optimistic lock to prevent concurrency conflicts
 * - Transaction management ensures data consistency
 * 
 * @Service: Identifies this as a Spring service layer component
 * @Slf4j: Lombok-provided logging annotation
 */
@Service
@Slf4j
public class UserService {
    
    

    
    
    
    private final UserRepository userRepository;        
    private final AddressRepository addressRepository;  
    private final CardRepository cardRepository;        
    private final RatingRepository ratingRepository;    
    
    
    private final KeycloakService keycloakService;      
    private final CloudinaryService cloudinaryService;  
    private final JwtTokenProvider jwtTokenProvider;    

    
    @Value("${cloudinary.avatar-upload-folder}")
    private String imageFolder;                         

    /**
     * Constructor dependency injection
     * Spring automatically injects all required dependency components
     * 
     * @param userRepository User data access layer
     * @param addressRepository Address data access layer
     * @param keycloakService Keycloak identity authentication service
     * @param cloudinaryService Cloudinary cloud storage service
     * @param jwtTokenProvider JWT token provider
     * @param cardRepository Payment card data access layer
     * @param ratingRepository Rating data access layer
     */
    @Autowired
    public UserService(UserRepository userRepository,
                       AddressRepository addressRepository,
                       KeycloakService keycloakService,
                       CloudinaryService cloudinaryService,
                       JwtTokenProvider jwtTokenProvider,
                       CardRepository cardRepository,
                       RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.keycloakService = keycloakService;
        this.cloudinaryService = cloudinaryService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cardRepository = cardRepository;
        this.ratingRepository = ratingRepository;
    }

    /**
     * Create new user
     * 
     * @param userDTO User registration information
     * @return JWT token for successful registration
     */
    @Transactional
    public String createUser(UserDTO userDTO) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("User with email " + userDTO.getEmail() + " already exists");
            }

            
            String adminToken = keycloakService.getAdminToken();
            keycloakService.createUserInKeycloak(adminToken, userDTO);

            
            String token = keycloakService.getUserToken(userDTO.getEmail(), userDTO.getPassword());

            
            Rating rating = new Rating();
            rating.setUserId(userDTO.getEmail());
            ratingRepository.save(rating);

            
            User user = UserMapper.toEntity(userDTO);
            user.setRating(rating);
            userRepository.save(user);

            return token;
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User information response
     */
    public ResponseDto fetchUserById(String id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserDTO userDTO = UserMapper.toDto(user);
            return new ResponseDto("200", "User fetched successfully", null, userDTO);
        } else {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    /**
     * Update user information
     * 
     * @param id User ID
     * @param userDTO Updated user information
     * @param profilePicture Profile picture file (optional)
     * @return Update success response
     */
    @Transactional
    public ResponseDto updateUser(String id, UserDTO userDTO, MultipartFile profilePicture) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        
        if (userDTO.getFirstName() != null) user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());

        
        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(profilePicture.getBytes(), imageFolder);
                user.setProfilePictureUrl(imageUrl);
            } catch (IOException e) {
                log.error("Error uploading profile picture", e);
                throw new RuntimeException("Failed to upload profile picture", e);
            }
        }

        userRepository.save(user);
        UserDTO updatedUserDTO = UserMapper.toDto(user);
        return new ResponseDto("200", "User updated successfully", null, updatedUserDTO);
    }

    /**
     * Delete user
     * 
     * @param id User ID
     * @return Deletion success response
     */
    @Transactional
    public ResponseDto deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        userRepository.delete(user);
        return new ResponseDto("200", "User deleted successfully", null, null);
    }

    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<ResponseDto> fetchAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    UserDTO userDTO = UserMapper.toDto(user);
                    return new ResponseDto("200", "User fetched successfully", null, userDTO);
                })
                .collect(Collectors.toList());
    }

    /**
     * Add payment card for user
     * 
     * @param userId User ID
     * @param cardDTO Payment card information
     * @return Card addition success response
     */
    @Transactional
    public ResponseDto addCard(String userId, CardDTO cardDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Card card = CardMapper.toEntity(cardDTO);
        card.setUser(user);
        cardRepository.save(card);

        CardDTO savedCardDTO = CardMapper.toDto(card);
        return new ResponseDto("200", "Card added successfully", null, savedCardDTO);
    }

    /**
     * Get user's payment cards
     * 
     * @param userId User ID
     * @return List of user's payment cards
     */
    public List<ResponseDto> getUserCards(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Card> cards = cardRepository.findByUser(user);
        return cards.stream()
                .map(card -> {
                    CardDTO cardDTO = CardMapper.toDto(card);
                    return new ResponseDto("200", "Card fetched successfully", null, cardDTO);
                })
                .collect(Collectors.toList());
    }

    /**
     * Delete payment card
     * 
     * @param userId User ID
     * @param cardId Card ID
     * @return Card deletion success response
     */
    @Transactional
    public ResponseDto deleteCard(String userId, String cardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Card does not belong to user");
        }

        cardRepository.delete(card);
        return new ResponseDto("200", "Card deleted successfully", null, null);
    }
}



