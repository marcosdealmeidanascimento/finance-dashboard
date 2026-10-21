package com.finances.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PT_BR);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final Clock clock;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, Clock clock) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.clock = clock;
    }

    public void sendHtmlEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
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
            String subject = "Bem-vindo ao Finance Dashboard";
            String text = buildWelcomeHtml(user);
            sendHtmlEmail(to, subject, text);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}",
                    user.getEmail(),
                    e);
        }
    }

    public String buildWelcomeHtml(User user) {
        Context context = new Context(PT_BR);
        context.setVariable("firstName", firstName(user.getName()));
        context.setVariable("name", user.getName());
        context.setVariable("email", user.getEmail());
        return templateEngine.process("email/welcome", context);
    }

    @Async("emailExecutor")
    public void sendMonthlySummaryNotification(SummaryResponse summary, UserResponse user) {
        try {
            String to = user.email();
            String subject = "Seu resumo de " + summaryMonth().format(MONTH);
            String text = buildMonthlySummaryHtml(summary, user);
            sendHtmlEmail(to, subject, text);
        } catch (Exception e) {
            log.error("Failed to send monthly summary email to {}",
                    user.email(),
                    e);
        }
    }

    public String buildMonthlySummaryHtml(SummaryResponse summary, UserResponse user) {
        BigDecimal income = summary.totalIncome();
        BigDecimal payments = summary.totalPayments();
        BigDecimal balance = summary.balance();

        Context context = new Context(PT_BR);
        context.setVariable("firstName", firstName(user.name()));
        context.setVariable("monthLabel", summaryMonth().format(MONTH));
        context.setVariable("totalIncome", currency(income));
        context.setVariable("totalPayments", currency(payments));
        context.setVariable("balance", currency(balance));
        context.setVariable("balanceSign", balance.signum());
        // Share of the income already spent, capped at 100 for the progress bar; null when there was no income.
        context.setVariable("spentPercent", income.signum() > 0
                ? payments.multiply(BigDecimal.valueOf(100)).divide(income, 0, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100)).intValue()
                : null);

        return templateEngine.process("email/monthly-summary", context);
    }

    @Async
    public void sendDueDateNotification(Payment payment) {
        String to = payment.getUser().getEmail();
        String subject = "Lembrete: " + payment.getDescription() + " vence " + dueLabel(payment.getDueDate());
        String text = buildDueDateHtml(payment);
        sendHtmlEmail(to, subject, text);
    }

    public String buildDueDateHtml(Payment payment) {
        Context context = new Context(PT_BR);
        context.setVariable("firstName", firstName(payment.getUser().getName()));
        context.setVariable("description", payment.getDescription());
        context.setVariable("amount", currency(payment.getAmount()));
        context.setVariable("dueDate", payment.getDueDate().format(DATE));
        context.setVariable("dueLabel", dueLabel(payment.getDueDate()));
        context.setVariable("method", payment.getMethod() == null || payment.getMethod().isBlank() ? "—" : payment.getMethod());
        context.setVariable("recurring", Boolean.TRUE.equals(payment.getRecurring()));

        return templateEngine.process("email/due-date", context);
    }

    /** The summary sent on the 1st covers the previous month (see SummaryService.getSummaryByUser). */
    private YearMonth summaryMonth() {
        return YearMonth.now(clock).minusMonths(1);
    }

    private String dueLabel(LocalDate dueDate) {
        LocalDate today = LocalDate.now(clock);
        if (dueDate.equals(today)) return "hoje";
        if (dueDate.equals(today.plusDays(1))) return "amanhã";
        return "em " + dueDate.format(DATE);
    }

    private static String currency(BigDecimal value) {
        // NumberFormat isn't thread-safe and these run on async executors, so one per call.
        return NumberFormat.getCurrencyInstance(PT_BR).format(value == null ? BigDecimal.ZERO : value);
    }

    private static String firstName(String name) {
        return name == null || name.isBlank() ? "" : name.trim().split("\\s+")[0];
    }

}
