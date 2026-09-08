package com.eventhub.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {


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

            System.err.println("⚠️ Simulando e-mail para [" + to + "]. Falha na conexão SMTP (Espera-se erro em Dev): " + e.getMessage());
        }
    }
}
