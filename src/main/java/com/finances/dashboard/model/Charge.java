package com.finances.dashboard.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "charges")
public class Charge extends DomainEntity {
    private String description;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Boolean recurring;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
