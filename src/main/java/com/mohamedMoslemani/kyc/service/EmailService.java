package com.mohamedMoslemani.kyc.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendKycUpdate(String to, String status) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("KYC Status Update");
        msg.setText("Your KYC status is now: " + status);
        mailSender.send(msg);

        log.info("KYC update email sent to {} with status {}", to, status);
    }

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Confirm your account");
        msg.setText("Click to verify your account: http://localhost:8080/api/auth/verify?token=" + token);
        mailSender.send(msg);

        log.info("Verification email sent to {} with token {}", to, token);
    }
}
