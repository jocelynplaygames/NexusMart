package com.example.NexusMart.service;
import com.example.NexusMart.dto.UserDTO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.master-id}")
    private String masterId;

    @Value("${keycloak.master-secret}")
    private String masterSecret;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    private final RestTemplate restTemplate;

    public KeycloakService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAdminToken() {
        String url = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", adminUsername);
        body.add("password", adminPassword);
        body.add("grant_type", "password");
        body.add("client_id", masterId);
        body.add("client_secret", masterSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Requesting admin token from Keycloak. URL: {}, Body: {}", url, body);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully retrieved admin token.");
                return (String) response.getBody().get("access_token");
            } else {
                log.error("Failed to retrieve admin token. Status: {}, Response: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to retrieve admin token from Keycloak.");
            }
        } catch (Exception e) {
            log.error("Exception occurred while retrieving admin token from Keycloak.", e);
            throw new RuntimeException("Exception occurred while retrieving admin token from Keycloak.", e);
        }
    }

    public void createUserInKeycloak(String token, UserDTO userDTO) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", userDTO.getPassword());
        credentials.put("temporary", false);

        Map<String, Object> body = new HashMap<>();
        body.put("username", userDTO.getEmail());
        body.put("email", userDTO.getEmail());
        body.put("enabled", true);
        body.put("credentials", new Map[] {credentials});

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Creating user in Keycloak. URL: {}, Body: {}", url, body);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("User created in Keycloak successfully.");
        } catch (Exception e) {
            log.error("Exception occurred while creating user in Keycloak.", e);
            throw new RuntimeException("Exception occurred while creating user in Keycloak.", e);
        }
    }

    public String getUserToken(String username, String password) {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", username);
        body.add("password", password);
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Requesting user token from Keycloak. URL: {}, Body: {}", url, body);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully retrieved user token.");
                return (String) response.getBody().get("access_token");
            } else {
                log.error("Failed to retrieve user token. Status: {}, Response: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to retrieve user token from Keycloak.");
            }
        } catch (Exception e) {
            log.error("Exception occurred while retrieving user token from Keycloak.", e);
            throw new RuntimeException("Exception occurred while retrieving user token from Keycloak.", e);
        }
    }

    public void updateUserInKeycloak(String adminToken, String userId, UserDTO userDTO) {
        String keycloakUrl = this.keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("username", userDTO.getUsername());
        body.put("email", userDTO.getEmail());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(keycloakUrl, HttpMethod.PUT, entity, String.class);
            log.info("Successfully updated user in Keycloak");
        } catch (Exception e) {
            log.error("Failed to update user in Keycloak", e);
            throw new RuntimeException("Failed to update user in Keycloak", e);
        }
    }
}
