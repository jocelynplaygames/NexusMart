package com.example.NexusMart.controller;
import com.example.NexusMart.dto.CardDTO;
import com.example.NexusMart.dto.ResponseDto;
import com.example.NexusMart.dto.UserDTO;
import com.example.NexusMart.exception.UserAlreadyExistsException;
import com.example.NexusMart.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping(path="/api/user", produces = "application/json")
@Validated
public class UserController {

    private final UserService userService;
    private final Environment environment;

    @Autowired
    public UserController(UserService userService, Environment environment) {
        this.userService = userService;
        this.environment = environment;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        log.info("User already registered with given email!");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody UserDTO userDTO) {
        String token = userService.createUser(userDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("token", token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> fetchUserById(@PathVariable String id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto("200", "User fetched successfully", null, userDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateUser(@PathVariable String id,
                                                  @RequestPart("user") String userJson,
                                                  @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserDTO userDTO = objectMapper.readValue(userJson, UserDTO.class);
            boolean isUpdated = userService.updateUser(id, userDTO, profilePicture);
            if (isUpdated) {
                UserDTO updatedUser = userService.getUserById(id); // Fetch the updated user
                log.info("Update successful!" + updatedUser);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new ResponseDto("200", "User updated successfully", null, updatedUser)); // Include updated user in response
            } else {
                return ResponseEntity
                        .status(HttpStatus.EXPECTATION_FAILED)
                        .body(new ResponseDto("417", "Failed to update user", null, null));
            }
        } catch (IOException e) {
            log.error("Error updating user profile picture", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto("500", "Error updating user profile picture", e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable String id) {
        boolean isDeleted = userService.deleteUserById(id);
        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto("200", "User deleted successfully", "", null));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto("417", "Failed to delete user", "", null));
        }
    }

    //card related
    @GetMapping("/{id}/card")
    public ResponseEntity<List<CardDTO>> getAllCards(@PathVariable String id) {
        List<CardDTO> cards = userService.getCardsByUserId(id);
        return ResponseEntity.status(HttpStatus.OK).body(cards);
    }

    @PostMapping("/{id}/card")
    public ResponseEntity<CardDTO> createCard(@PathVariable String id, @RequestBody CardDTO cardDTO) {
        CardDTO createdCard = userService.createCard(id, cardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @PutMapping("/{id}/card/{cardId}")
    public ResponseEntity<CardDTO> updateCard(@PathVariable String id, @PathVariable Long cardId, @RequestBody CardDTO cardDTO) {
        try {
            CardDTO updatedCard = userService.updateCard(cardId, cardDTO);
            return ResponseEntity.status(HttpStatus.OK).body(updatedCard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{id}/card/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id, @PathVariable Long cardId) {
        try {
            userService.deleteCard(cardId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    //testing
    @GetMapping("/java-version")
    public ResponseEntity<String> getJavaVersion() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(environment.getProperty("JAVA_HOME"));
    }
}
