package com.eventhub.api.controller;

import com.eventhub.api.dto.PasswordUpdateRequestDTO;
import com.eventhub.api.dto.UserUpdateRequestDTO;
import com.eventhub.api.model.User;
import com.eventhub.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(
            @Valid @RequestBody UserUpdateRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.updateProfile(request, user));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody PasswordUpdateRequestDTO request,
            @AuthenticationPrincipal User user) {
        userService.updatePassword(request, user);
        return ResponseEntity.noContent().build();
    }
}
