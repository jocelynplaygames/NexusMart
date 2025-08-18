package com.example.NexusMart;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JWKSParserTest {

    public static void main(String[] args) {
        String jwksUri = "http:
        try {
            PublicKey publicKey = getPublicKeyFromJWKS(jwksUri);
            System.out.println("Public Key: " + publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PublicKey getPublicKeyFromJWKS(String jwksUri) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(jwksUri, String.class);
            System.out.println("JWKS Response: " + response);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);

            
            JsonNode keyNode = null;
            for (JsonNode key : jsonNode.get("keys")) {
                if ("sig".equals(key.get("use").asText()) && "RS256".equals(key.get("alg").asText())) {
                    keyNode = key;
                    break;
                }
            }

            if (keyNode == null) {
                throw new IllegalArgumentException("No suitable key found in JWKS");
            }

            String publicKeyString = keyNode.get("x5c").get(0).asText();
            System.out.println("Public Key String: " + publicKeyString);

            
            byte[] decoded = Base64.getDecoder().decode(publicKeyString);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new java.io.ByteArrayInputStream(decoded));
            return certificate.getPublicKey();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid public key provided", e);
        }
    }
}