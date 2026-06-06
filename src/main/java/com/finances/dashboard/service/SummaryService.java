package com.finances.dashboard.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finances.dashboard.dto.response.SummaryResponse;
import com.finances.dashboard.dto.response.SummaryUserResponse;
import com.finances.dashboard.dto.response.UserResponse;
import com.finances.dashboard.mapper.UserMapper;
import com.finances.dashboard.repository.IncomeRepository;
import com.finances.dashboard.repository.PaymentRepository;
import com.finances.dashboard.repository.SummaryRepository;
import com.finances.dashboard.repository.UserRepository;

@Service
public class SummaryService {
    private final IncomeRepository incomeRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SummaryRepository summaryRepository;
    private final UserMapper userMapper;

    public SummaryService(IncomeRepository incomeRepository,
            PaymentRepository paymentRepository, UserRepository userRepository, SummaryRepository summaryRepository,
            UserMapper userMapper) {
        this.incomeRepository = incomeRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.summaryRepository = summaryRepository;
        this.userMapper = userMapper;
    }

    public SummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = incomeRepository.sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(userId,
                startDate, endDate) != null
                        ? incomeRepository.sumByUser_IdAndDeletedAtIsNullAndReceivedDateBetween(userId,
                                startDate, endDate)
                        : BigDecimal.ZERO;
        BigDecimal totalPayments = paymentRepository.sumByUser_IdAndDeletedAtIsNullAndDueDateBetween(userId, startDate,
                endDate) != null ? paymentRepository.sumByUser_IdAndDeletedAtIsNullAndDueDateBetween(userId, startDate,
                        endDate) : BigDecimal.ZERO;
        BigDecimal balance = totalIncome.subtract(totalPayments);
        return new SummaryResponse(totalIncome, totalPayments, balance);
    }

    public List<SummaryUserResponse> getSummaryByUser() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(1).withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(1).minusDays(1);

        return summaryRepository
                .findAllSummaries(startDate, endDate)
                .stream()
                .map(p -> {

                    SummaryResponse summary = new SummaryResponse(
                            p.totalIncome(),
                            p.totalPayments(),
                            p.totalIncome()
                                    .subtract(p.totalPayments()));

                    UserResponse user = new UserResponse(
                            p.userId(),
                            p.name(),
                            p.email());

                    return new SummaryUserResponse(
                            summary,
                            user);
                })
                .toList();
    }

}
