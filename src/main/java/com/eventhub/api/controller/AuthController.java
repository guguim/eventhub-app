package com.eventhub.api.controller;

import com.eventhub.api.dto.AuthRequestDTO;
import com.eventhub.api.dto.AuthResponseDTO;
import com.eventhub.api.dto.RegisterRequestDTO;
import com.eventhub.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Lembra que nós liberamos "/api/auth/*" no SecurityConfig?
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Rota pública para criar uma nova conta
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Rota pública para fazer login e pegar o Token JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
