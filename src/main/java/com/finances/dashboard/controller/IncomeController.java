package com.finances.dashboard.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.dto.request.IncomeCreateRequest;
import com.finances.dashboard.dto.request.IncomeUpdateRequest;
import com.finances.dashboard.dto.response.IncomeResponse;
import com.finances.dashboard.exception.BadRequestException;
import com.finances.dashboard.mapper.IncomeMapper;
import com.finances.dashboard.service.IncomeService;
import com.finances.dashboard.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

// Ownership lives in IncomeService: every lookup is scoped to the authenticated user.
@RestController
@RequestMapping("api/incomes")
@SecurityRequirement(name = "bearerAuth")
public class IncomeController {
    private final IncomeService incomeService;
    private final UserService userService;
    private final IncomeMapper incomeMapper;

    public IncomeController(IncomeService incomeService, UserService userService, IncomeMapper incomeMapper) {
        this.incomeService = incomeService;
        this.userService = userService;
        this.incomeMapper = incomeMapper;
    }

    @GetMapping
    public ResponseEntity<Page<IncomeResponse>> getCurrentUserIncomes(Pageable pageable,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal Long userId) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("A data inicial deve ser anterior à data final.");
        }
        return ResponseEntity.ok(
                incomeService.findByUser_Id(userId, startDate, endDate, pageable).map(incomeMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncome(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(incomeMapper.toResponse(incomeService.getOwned(id, userId)));
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(@Valid @RequestBody IncomeCreateRequest incomeRequest,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(incomeMapper.toResponse(
                incomeService.create(incomeRequest, userService.getActive(userId))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(@PathVariable Long id,
            @Valid @RequestBody IncomeUpdateRequest incomeRequest,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(incomeMapper.toResponse(incomeService.update(id, userId, incomeRequest)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        incomeService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

}
