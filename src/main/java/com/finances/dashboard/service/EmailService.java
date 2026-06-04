package com.finances.dashboard.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.finances.dashboard.model.Payment;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    public void sendDueDateNotification(Payment payment) {
        String emailTo = payment.getUser().getEmail();
        String emailSubject = "Payment due date";
        String emailBody = buildDueDateBody(payment);
        sendEmail(emailTo, emailSubject, emailBody);
    }

    private String buildDueDateBody(Payment payment) {
        return "Your payment for "
                + payment.getDescription() + " - " + payment.getId()
                + " is due on "
                + payment.getDueDate()
                + ". Please make the payment as soon as possible.";
    }

}
