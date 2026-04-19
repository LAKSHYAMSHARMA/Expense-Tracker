package com.expenseTracker.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.expenseTracker.dto.UserDTO;
import com.expenseTracker.entities.User;
import com.expenseTracker.exception.BusinessException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    public UserDTO getUserById(Integer userId) {
        log.info("Fetching user with id: {}", userId);

        if (userId == null || userId <= 0) {
            throw new BusinessException("Invalid user ID provided");
        }

        return userRepository.findById(userId)
                .map(user -> modelMapper.map(user, UserDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public UserDTO getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);

        if (email == null || email.isBlank()) {
            throw new BusinessException("Email cannot be empty");
        }

        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public UserDTO createUser(UserDTO inputUser) {
        log.info("Creating new user with email: {}", inputUser.getEmail());

        if (userRepository.findByEmail(inputUser.getEmail()).isPresent()) {
            throw new BusinessException("User with this email already exists");
        }

        User toSaveEntity = modelMapper.map(inputUser, User.class);
        toSaveEntity.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(toSaveEntity);
        log.info("User created successfully with id: {}", savedUser.getId());

        return modelMapper.map(savedUser, UserDTO.class);
    }

    public UserDTO signInWithGoogle(String idToken) {
        log.info("Signing in user with Google token");

        if (idToken == null || idToken.isBlank()) {
            throw new BusinessException("Google ID token cannot be empty");
        }

        if (googleClientId == null || googleClientId.isBlank()) {
            throw new BusinessException("Google OAuth client id is not configured on server");
        }

        GoogleIdToken.Payload payload = verifyGoogleIdToken(idToken);

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        Boolean emailVerified = payload.getEmailVerified();

        if (email == null || email.isBlank()) {
            throw new BusinessException("Google account email is missing");
        }

        if (emailVerified == null || !emailVerified) {
            throw new BusinessException("Google account email is not verified");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(name, email));

        if (name != null && !name.isBlank() && !name.equals(user.getName())) {
            user.setName(name);
            user = userRepository.save(user);
        }

        return modelMapper.map(user, UserDTO.class);
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(List.of(googleClientId)).build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new BusinessException("Invalid Google ID token");
            }

            return token.getPayload();
        } catch (GeneralSecurityException ex) {
            throw new BusinessException("Unable to validate Google token securely");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Google token verification failed");
        }
    }

    private User createGoogleUser(String name, String email) {
        User toSave = User.builder()
                .name((name == null || name.isBlank()) ? "Google User" : name)
                .email(email)
                .password(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(toSave);
        log.info("Created new Google user with id: {}", savedUser.getId());
        return savedUser;
    }
}
