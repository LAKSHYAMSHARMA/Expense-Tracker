package com.expenseTracker.controllers;

import com.expenseTracker.dto.ApiResponse;
import com.expenseTracker.dto.GoogleAuthRequestDTO;
import com.expenseTracker.dto.UserDTO;
import com.expenseTracker.services.UserService;
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
public class AuthController {

    private final UserService userService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<UserDTO>> signInWithGoogle(@Valid @RequestBody GoogleAuthRequestDTO request) {
        log.info("Handling Google sign-in");
        UserDTO user = userService.signInWithGoogle(request.getIdToken());
        return ResponseEntity.ok(ApiResponse.success(user, "Google sign-in successful", HttpStatus.OK.value()));
    }
}
