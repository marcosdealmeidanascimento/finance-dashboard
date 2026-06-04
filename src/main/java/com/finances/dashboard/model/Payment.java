package com.finances.dashboard.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import com.finances.dashboard.enums.PaymentStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Data
@Table(name = "payments")
public class Payment extends DomainEntity {
    private String description;
    private String method;
    private Boolean recurring;
    private BigDecimal amount;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Boolean isCloseToDueDate() {
        return this.getDueDate().isAfter(LocalDate.now().minusDays(1)) && this.getDueDate().isBefore(LocalDate.now().plusDays(2));
    }

    public Boolean isRecurring() {
        return this.getRecurring();
    }

}
