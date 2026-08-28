package com.eventhub.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity // Habilita a configuração de segurança Web para o Spring Boot
@RequiredArgsConstructor
public class SecurityConfig {

    private final com.eventhub.api.security.JwtAuthenticationFilter jwtAuthFilter;

    // 1. Filtro Central de Segurança (A barreira de entrada da nossa API)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita o CSRF (falsificação de solicitação entre sites). 
                // Em APIs REST com JWT, nós não usamos a sessão do navegador, então estamos imunes a esse tipo de ataque.
                .csrf(AbstractHttpConfigurer::disable)
                
                // Define a API como STATELESS (Sem Estado). O servidor não vai lembrar de quem você é entre uma requisição e outra.
                // Você terá que provar quem é mandando o Token JWT TODA VEZ.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configuração das fechaduras das nossas portas (Rotas)
                .authorizeHttpRequests(auth -> auth
                        // Liberamos (permitAll) o acesso para as rotas de cadastro e login. Qualquer um pode acessar!
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        
                        // Bloqueamos qualquer outra rota (ex: /api/events). Só entra quem estiver autenticado!
                        .anyRequest().authenticated()
                )
                // Encaixamos o nosso pedágio (Filtro JWT) antes do filtro padrão de Login do Spring!
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // 2. O Criptografador
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usamos BCrypt. Ele pega uma senha como "123456" e transforma num embaralhado irreversível gigante, 
        // impossível de decifrar até para o dono do banco de dados.
        return new BCryptPasswordEncoder();
    }

    // 3. O Gerente de Autenticação
    // Precisamos expor esse carinha como um Bean. Ele é quem pega um usuário e senha, 
    // vai no banco e confere se a senha bate usando o nosso passwordEncoder.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
