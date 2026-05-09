package com.expenseTracker.controllers;

import com.expenseTracker.dto.ApiResponse;
import com.expenseTracker.dto.AuthResponseDTO;
import com.expenseTracker.dto.GoogleAuthRequestDTO;
import com.expenseTracker.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final UserService userService;

    @PostMapping("/google")
    @Operation(summary = "Sign in with Google OAuth token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> signInWithGoogle(@Valid @RequestBody GoogleAuthRequestDTO request) {
        log.info("Handling Google sign-in request");
        AuthResponseDTO authResponse = userService.signInWithGoogle(request.getIdToken());
        log.info("Google sign-in successful for userId={}", authResponse.getUserId());
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Google sign-in successful", HttpStatus.OK.value()));
    }
}
