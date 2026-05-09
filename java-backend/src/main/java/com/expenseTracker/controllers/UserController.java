package com.expenseTracker.controllers;

import com.expenseTracker.dto.ApiResponse;
import com.expenseTracker.dto.UserDTO;
import com.expenseTracker.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Integer userId) {
        log.info("Fetching user by id={}", userId);
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully", HttpStatus.OK.value()));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user by email={}", email);
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully", HttpStatus.OK.value()));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO inputUser) {
        log.info("Creating user with email={}", inputUser.getEmail());
        UserDTO savedUser = userService.createUser(inputUser);
        log.info("User created successfully with id={}", savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedUser, "User created successfully", HttpStatus.CREATED.value()));
    }
}