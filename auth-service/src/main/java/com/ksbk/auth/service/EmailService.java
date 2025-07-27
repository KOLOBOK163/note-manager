package com.ksbk.auth.service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

    private JavaMailSender mailSender;

    public void sendMail(String email, String subject, String body)
    {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("example@example.com");
        mail.setTo(email);
        mail.setSubject(subject);
        mail.setText(body);

        mailSender.send(mail);
    }
}