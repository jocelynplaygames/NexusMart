package com.example.NexusMart.service;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final KeycloakService keycloakService;
    private final CloudinaryService cloudinaryService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CardRepository cardRepository;

    private final RatingRepository ratingRepository;

    @Value("${cloudinary.avatar-upload-folder}")
    private String imageFolder;

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

    public String createUser(UserDTO userDTO) {
        User user = UserMapper.toEntity(userDTO);
        Optional<User> optionalUser = userRepository.findByEmail(userDTO.getEmail());
        if (optionalUser.isPresent()) {
            log.info("User already registered with given email!");
            throw new UserAlreadyExistsException("User already registered with given email: " + userDTO.getEmail());
        }

        // Register user in Keycloak
        String adminToken = keycloakService.getAdminToken();
        keycloakService.createUserInKeycloak(adminToken, userDTO);
        String token = keycloakService.getUserToken(userDTO.getEmail(), userDTO.getPassword());

        // Extract the user ID from the token
        String keycloakUserId = jwtTokenProvider.extractUserIdFromToken(token);
        log.info("Extracted Keycloak user ID: {}", keycloakUserId);
        user.setId(keycloakUserId); // Ensure the ID is set correctly

        Rating rating = new Rating();
        rating.setEntityId(keycloakUserId);
        rating.setEntityType(Rating.EntityType.SELLER);
        rating.setTotalRating(0);
        rating.setNumRatings(0);
        ratingRepository.save(rating);
        log.info("Created empty rating for user with ID: {}", user.getId());

        userRepository.save(user);
        log.info("Successfully created a user with ID: {}", user.getId());
        return token;
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email)
        );
        return UserMapper.toDTO(user);
    }

    public UserDTO getUserById(String id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );
        return UserMapper.toDTO(user);
    }

    public boolean updateUser(String id, UserDTO userDTO, MultipartFile profilePicture) throws IOException {
        User existingUser = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );

        if(Objects.equals(userDTO.getEmail(), "")){
            throw new RuntimeException("Email is not valid.");
        }

        log.info("Updating user with ID: {}", id);
        log.info("Existing user: {}", existingUser);
        log.info("New user's ProfilePictureUrl", userDTO.getProfilePictureUrl());

        boolean emailChanged = !existingUser.getEmail().equals(userDTO.getEmail());
        boolean usernameChanged = !existingUser.getUsername().equals(userDTO.getUsername());

        // Check if the new email is already used by another user
        if (emailChanged) {
            Optional<User> userWithEmail = userRepository.findByEmail(userDTO.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                throw new UserAlreadyExistsException("Email is already in use by another user.");
            }
        }

        UserMapper.updateEntityFromDTO(userDTO, existingUser);
        log.info("Updated user entity: {}", existingUser.getProfilePictureUrl());

        if (userDTO.getAddress() != null) {
            Address address = existingUser.getAddress() != null ? existingUser.getAddress() : new Address();
            address.setUser(existingUser);
            address.setStreet(userDTO.getAddress().getStreet());
            address.setCity(userDTO.getAddress().getCity());
            address.setState(userDTO.getAddress().getState());
            address.setPostalCode(userDTO.getAddress().getPostalCode());
            address.setCountry(userDTO.getAddress().getCountry());
            log.info("Saving address: {}", address);
            addressRepository.save(address);
            existingUser.setAddress(address);
        }

        if (profilePicture != null && !profilePicture.isEmpty()) {
            // Delete the old avatar if it exists
            if (existingUser.getProfilePictureUrl() != null && !existingUser.getProfilePictureUrl().isEmpty()) {
                log.info("Current profile picture URL: {}", existingUser.getProfilePictureUrl());
                String publicId = cloudinaryService.extractPublicIdFromUrl(existingUser.getProfilePictureUrl());
                log.info("Deleting old avatar with public ID: {}", publicId);
                cloudinaryService.deleteFile(publicId, imageFolder);
            }
            // Upload the new avatar
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(profilePicture.getBytes(), imageFolder);
            existingUser.setProfilePictureUrl((String) uploadResult.get("url"));
        }

        log.info("Saving user: {}", existingUser);
        userRepository.save(existingUser);

        if (emailChanged || usernameChanged) {
            String adminToken = keycloakService.getAdminToken();
            keycloakService.updateUserInKeycloak(adminToken, id, userDTO);
        }

        return true;
    }

    public boolean deleteUserById(String id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );
        userRepository.deleteById(user.getId());
        return true;
    }

    //cards related
    public List<CardDTO> getCardsByUserId(String userId) {
        List<Card> cards = cardRepository.findByUserId(userId);
        return cards.stream().map(CardMapper::toDTO).collect(Collectors.toList());
    }

    public CardDTO createCard(String userId, CardDTO cardDTO) {
        log.info("creating card! User id: " + userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Card card = CardMapper.toEntity(cardDTO);
        card.setUser(user);
        log.info("saving card!");
        Card savedCard = cardRepository.save(card);
        return CardMapper.toDTO(savedCard);
    }

    public CardDTO updateCard(Long cardId, CardDTO cardDTO) {
        Card existingCard = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId.toString()));
        existingCard.setType(cardDTO.getType());
        existingCard.setCardNumber(cardDTO.getCardNumber());
        existingCard.setExpirationDate(cardDTO.getExpirationDate());
        Card updatedCard = cardRepository.save(existingCard);
        return CardMapper.toDTO(updatedCard);
    }

    public void deleteCard(Long cardId) {
        Card existingCard = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId.toString()));
        cardRepository.delete(existingCard);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }
}


