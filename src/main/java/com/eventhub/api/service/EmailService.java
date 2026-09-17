package com.eventhub.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;

    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("nao-responda@eventhub.com");

            mailSender.send(message);
            log.info("✅ E-mail real enviado com sucesso para: {}", to);

        } catch (Exception e) {

            log.warn("⚠️ Simulando e-mail para [{}]. Falha na conexão SMTP (Espera-se erro em Dev): {}", to, e.getMessage());
        }
    }
}
