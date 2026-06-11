package com.finances.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.finances.dashboard.dto.response.SummaryResponse;
import com.finances.dashboard.dto.response.UserResponse;
import com.finances.dashboard.model.Payment;
import com.finances.dashboard.model.User;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendHtmlEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendWelcomeEmail(User user) {
        try {
            String to = user.getEmail();
            String subject = "Welcome to Finances Dashboard";
            String text = buildWelcomeHtml(user);
            sendHtmlEmail(to, subject, text);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}",
                    user.getEmail(),
                    e);
        }
    }

    public String buildWelcomeHtml(User user) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("email", user.getEmail());
        return templateEngine.process("email/welcome", context);
    }

    @Async("emailExecutor")
    public void sendMonthlySummaryNotification(SummaryResponse summary, UserResponse user) {
        try {
            String to = user.email();
            String subject = "Monthly Summary";
            String text = buildMonthlySummaryHtml(summary, user);
            sendHtmlEmail(to, subject, text);
        } catch (Exception e) {
            log.error("Failed to send monthly summary email to {}",
                    user.email(),
                    e);
        }
    }

    public String buildMonthlySummaryHtml(SummaryResponse summary, UserResponse user) {
        Context context = new Context();
        context.setVariable("name", user.name());
        context.setVariable("totalIncome", summary.totalIncome());
        context.setVariable("totalPayments", summary.totalPayments());
        context.setVariable("balance", summary.balance());

        return templateEngine.process("email/monthly-summary", context);
    }

    @Async
    public void sendDueDateNotification(Payment payment) {
        String to = payment.getUser().getEmail();
        String subject = "Payment Due Reminder";
        String text = buildDueDateHtml(payment);
        sendHtmlEmail(to, subject, text);
    }

    public String buildDueDateHtml(Payment payment) {
        Context context = new Context();
        context.setVariable("name", payment.getUser().getName());
        context.setVariable("description", payment.getDescription());
        context.setVariable("amount", payment.getAmount());
        context.setVariable("id", payment.getId());
        context.setVariable("dueDate", payment.getDueDate());

        return templateEngine.process("email/due-date", context);
    }

}
