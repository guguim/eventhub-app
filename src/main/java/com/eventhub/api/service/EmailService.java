package com.eventhub.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    // O "carteiro" oficial do JavaMail. O Spring Boot já configura isso automaticamente lendo o application.properties!
    private final JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("nao-responda@eventhub.com");

            mailSender.send(message);
            System.out.println("✅ E-mail real enviado com sucesso para: " + to);
            
        } catch (Exception e) {
            // Como em ambiente de desenvolvimento nós configuramos credenciais falsas no application.properties,
            // o disparo vai cair neste catch. E isso é de propósito! Assim evitamos que a API inteira trave (Crash 500)
            // só porque o servidor SMTP fictício não respondeu.
            System.err.println("⚠️ Simulando e-mail para [" + to + "]. Falha na conexão SMTP (Espera-se erro em Dev): " + e.getMessage());
        }
    }
}
