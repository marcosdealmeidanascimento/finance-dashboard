package com.finances.dashboard.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.response.SummaryResponse;
import com.finances.dashboard.dto.response.SummaryUserResponse;
import com.finances.dashboard.dto.response.UserResponse;
import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.IncomeRepository;
import com.finances.dashboard.repository.PaymentRepository;
import com.finances.dashboard.repository.UserRepository;

@Service
public class SummaryService {
    private final IncomeRepository incomeRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public SummaryService(IncomeRepository incomeRepository,
            PaymentRepository paymentRepository, UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    public SummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = incomeRepository.sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(userId,
                startDate, endDate) != null ? incomeRepository.sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(userId,
                startDate, endDate) : BigDecimal.ZERO;
        BigDecimal totalPayments = paymentRepository.sumByUser_IdAndDeletedAtIsNullAndDueDateBetween(userId, startDate,
                endDate) != null ? paymentRepository.sumByUser_IdAndDeletedAtIsNullAndDueDateBetween(userId, startDate,
                endDate) : BigDecimal.ZERO;
        BigDecimal balance = totalIncome.subtract(totalPayments);
        return new SummaryResponse(totalIncome, totalPayments, balance);
    }

    public List<SummaryUserResponse> getSummaryByUser() {
        List<User> users = userRepository.findAllAndDeletedAtIsNull();

        List<SummaryUserResponse> response = new ArrayList<>();

        for (User user : users) {
            SummaryResponse summary = getSummary(
                    user.getId(),
                    LocalDate.now().minusMonths(1),
                    LocalDate.now());

            response.add(
                    new SummaryUserResponse(summary, new UserResponse(user.getId(), user.getName(), user.getEmail())));
        }

        return response;
    }

}
