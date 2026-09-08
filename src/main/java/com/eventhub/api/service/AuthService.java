package com.eventhub.api.service;

import com.eventhub.api.dto.AuthRequestDTO;
import com.eventhub.api.dto.AuthResponseDTO;
import com.eventhub.api.dto.RegisterRequestDTO;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.UserRepository;
import com.eventhub.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado na plataforma");
        }


        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());


        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);


        String jwtToken = jwtService.generateToken(user);

        return new AuthResponseDTO(jwtToken, user.getId(), user.getName());
    }

    public AuthResponseDTO login(AuthRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );


        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponseDTO(jwtToken, user.getId(), user.getName());
    }
}
