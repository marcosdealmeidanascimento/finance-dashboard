package com.finances.dashboard.tasks;

import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.finances.dashboard.model.Payment;
import com.finances.dashboard.service.EmailService;
import com.finances.dashboard.service.PaymentService;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private final EmailService emailService;
    private final PaymentService paymentService;

    public ScheduledTasks(EmailService emailService, PaymentService paymentService) {
        this.emailService = emailService;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedRate = 15000)
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(System.currentTimeMillis()));
    }

    @Scheduled(cron = "0 0 14 * * *")
    public void validatePayments() {
        log.info("Validating payments");
        List<Payment> payments = paymentService.listCloseToDueDatePayments();
        log.info("Found {} payments to validate", payments.size());
        payments.forEach(emailService::sendDueDateNotification);
    }
}
