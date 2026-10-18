package com.finances.dashboard.tasks;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.finances.dashboard.dto.response.SummaryUserResponse;
import com.finances.dashboard.model.Payment;
import com.finances.dashboard.service.EmailService;
import com.finances.dashboard.service.PaymentService;
import com.finances.dashboard.service.SummaryService;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final EmailService emailService;
    private final PaymentService paymentService;
    private final SummaryService summaryService;

    public ScheduledTasks(EmailService emailService, PaymentService paymentService, SummaryService summaryService) {
        this.emailService = emailService;
        this.paymentService = paymentService;
        this.summaryService = summaryService;
    }

    @Async
    @Scheduled(cron = "0 0 03 * * *")
    public void validatePayments() {
        log.info("Validating payments");
        List<Payment> payments = paymentService.listCloseToDueDatePayments();
        log.info("Found {} payments to validate", payments.size());
        payments.forEach(emailService::sendDueDateNotification);
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void sendMonthlySummary() {
        log.info("Sending monthly summary");
        List<SummaryUserResponse> summaries = summaryService.getSummaryByUser();
        for (SummaryUserResponse summary : summaries) {
            emailService.sendMonthlySummaryNotification(
                    summary.summary(),
                    summary.user());
        }
        log.info("All emails queued");
    }
}
