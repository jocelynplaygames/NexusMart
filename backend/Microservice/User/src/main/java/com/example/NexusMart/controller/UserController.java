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








/**
 * User Management Controller
 * Responsible for handling user-related HTTP requests, including user CRUD operations, payment card management, etc.
 * 
 * @Slf4j: Lombok-provided logging annotation, automatically generates log variable
 * @RestController: Identifies this as a REST API controller, returns JSON format data
 * @RequestMapping: Defines API base path and response format
 * @Validated: Enables method-level parameter validation
 */
@Slf4j
@RestController
@RequestMapping(path="/api/user", produces = "application/json")
@Validated
public class UserController {

    
    private final UserService userService;
    
    private final Environment environment;

    /**
     * Constructor dependency injection
     * @param userService User business service
     * @param environment Spring environment configuration
     */
    @Autowired
    public UserController(UserService userService, Environment environment) {
        this.userService = userService;
        this.environment = environment;
    }

    /**
     * Global exception handler: handles user already exists exception
     * Triggered when user registration email already exists
     * 
     * @param ex User already exists exception object
     * @return HTTP 409 Conflict response with error message
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        log.info("User already registered with given email!");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Create new user
     * HTTP POST /api/user
     * 
     * @param userDTO User data transfer object containing user registration information
     * @return Creation success response with JWT token
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody UserDTO userDTO) {
        String token = userService.createUser(userDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("token", token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get user by ID
     * HTTP GET /api/user/{id}
     * 
     * @param id User ID
     * @return User information response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> fetchUserById(@PathVariable String id) {
        ResponseDto responseDto = userService.fetchUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /**
     * Update user information
     * HTTP PUT /api/user/{id}
     * 
     * @param id User ID
     * @param userJson User information JSON string
     * @param profilePicture Profile picture file (optional)
     * @return Update success response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateUser(@PathVariable String id, 
                                                 @RequestPart("user") String userJson, 
                                                 @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserDTO userDTO = objectMapper.readValue(userJson, UserDTO.class);
            ResponseDto responseDto = userService.updateUser(id, userDTO, profilePicture);
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (IOException e) {
            log.error("Error parsing user JSON", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Delete user
     * HTTP DELETE /api/user/{id}
     * 
     * @param id User ID
     * @return Deletion success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable String id) {
        ResponseDto responseDto = userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /**
     * Get all users
     * HTTP GET /api/user
     * 
     * @return List of all users
     */
    @GetMapping
    public ResponseEntity<List<ResponseDto>> fetchAllUsers() {
        List<ResponseDto> responseDtos = userService.fetchAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(responseDtos);
    }

    /**
     * Add payment card
     * HTTP POST /api/user/{id}/cards
     * 
     * @param id User ID
     * @param cardDTO Payment card information
     * @return Card addition success response
     */
    @PostMapping("/{id}/cards")
    public ResponseEntity<ResponseDto> addCard(@PathVariable String id, @Valid @RequestBody CardDTO cardDTO) {
        ResponseDto responseDto = userService.addCard(id, cardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Get user's payment cards
     * HTTP GET /api/user/{id}/cards
     * 
     * @param id User ID
     * @return List of user's payment cards
     */
    @GetMapping("/{id}/cards")
    public ResponseEntity<List<ResponseDto>> getUserCards(@PathVariable String id) {
        List<ResponseDto> responseDtos = userService.getUserCards(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseDtos);
    }

    /**
     * Delete payment card
     * HTTP DELETE /api/user/{id}/cards/{cardId}
     * 
     * @param id User ID
     * @param cardId Card ID
     * @return Card deletion success response
     */
    @DeleteMapping("/{id}/cards/{cardId}")
    public ResponseEntity<ResponseDto> deleteCard(@PathVariable String id, @PathVariable String cardId) {
        ResponseDto responseDto = userService.deleteCard(id, cardId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /**
     * Get application status
     * HTTP GET /api/user/status
     * 
     * @return Application status information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        Map<String, String> status = new HashMap<>();
        status.put("message", "Working");
        status.put("port", environment.getProperty("local.server.port"));
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
}
