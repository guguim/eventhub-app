package com.eventhub.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Essa classe é um "pedágio" que vai rodar UMA VEZ a cada requisição (OncePerRequestFilter)
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Procuramos se a pessoa mandou a carteirinha (Header Authorization)
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Se ela não mandou, ou mandou no formato errado, a gente passa ela pra frente (e aí o Spring bloqueia!)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraímos só o token (tirando a palavra "Bearer " do começo)
        jwt = authHeader.substring(7);
        
        try {
            // 3. Olhamos quem é o dono do token (o e-mail dele)
            userEmail = jwtService.extractUsername(jwt);
            
            // 4. Se ele não está autenticado nessa requisição ainda...
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Vamos buscar ele no banco
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                // E conferir se a carteirinha (token) é realmente válida e não expirou
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    // Se for válida, nós "liberamos a catraca" (criamos o token de autenticação interno do Spring)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Senha não importa mais aqui
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Salvamos na memória de segurança dessa requisição
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Se der erro ao ler o token (ex: expirou), deixamos seguir sem autenticar. O Spring vai bloquear.
        }
        
        // Segue a vida para o próximo filtro ou controller
        filterChain.doFilter(request, response);
    }
}
