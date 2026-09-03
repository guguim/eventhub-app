package com.eventhub.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Habilita o servidor de WebSocket na aplicação
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "enableSimpleBroker": Cria o nosso rádio-transmissor. 
        // Tudo que o servidor mandar para "/topic/..." será distribuído para todos os clientes escutando.
        config.enableSimpleBroker("/topic");
        
        // "setApplicationDestinationPrefixes": Define o prefixo caso o Frontend queira enviar algo para o Backend via WS.
        // No nosso projeto o Frontend usará HTTP para enviar (POST/PATCH), e o Backend usará WS para avisar.
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // "/ws" é a porta de entrada. É a URL que o Frontend vai chamar para iniciar a conexão túnel.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permite o Frontend (React) conectar sem erro de CORS
                .withSockJS(); // Adiciona suporte para navegadores mais antigos que não suportam WebSocket nativo
    }
}
