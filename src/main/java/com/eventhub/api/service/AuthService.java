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
        // Verifica se o e-mail já existe no banco para não duplicar
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado na plataforma");
        }

        // Montamos a Entidade User
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        
        // CUIDADO MÁXIMO: Criptografar a senha ANTES de salvar!
        // passwordEncoder.encode() vai gerar um hash irreversível.
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        // Gera o token para o novo usuário já sair logado!
        String jwtToken = jwtService.generateToken(user);
        
        return new AuthResponseDTO(jwtToken);
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        // O AuthenticationManager faz o trabalho mágico aqui.
        // Ele vai lá no UserDetailsService que criamos, busca o usuário, 
        // e compara a senha enviada (plana) com o Hash do banco de dados!
        // Se estiver errada, ele quebra a execução e lança uma BadCredentialsException.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Se a linha de cima não deu erro, a senha está correta! 
        // Agora só precisamos buscar os dados do usuário para colocar no Token.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String jwtToken = jwtService.generateToken(user);
        
        return new AuthResponseDTO(jwtToken);
    }
}
