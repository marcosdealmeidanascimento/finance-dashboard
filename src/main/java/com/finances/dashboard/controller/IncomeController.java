package com.finances.dashboard.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finances.dashboard.dto.request.IncomeCreateRequest;
import com.finances.dashboard.dto.request.IncomeUpdateRequest;
import com.finances.dashboard.dto.response.IncomeResponse;
import com.finances.dashboard.mapper.IncomeMapper;
import com.finances.dashboard.model.Income;
import com.finances.dashboard.model.User;
import com.finances.dashboard.service.IncomeService;
import com.finances.dashboard.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

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
    public ResponseEntity<Page<IncomeResponse>> getCurrentUserIncomes(Pageable pageable, LocalDate startDate,
            LocalDate endDate,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Page<Income> incomes = incomeService.findByUser_Id(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(incomes.map(incomeMapper::toResponse));
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(@RequestBody IncomeCreateRequest incomeRequest,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            User user = userService.findById(userId);
            Income createdIncome = incomeService.create(incomeRequest, user);
            return ResponseEntity.ok(incomeMapper.toResponse(createdIncome));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(@PathVariable Long id,
            @RequestBody IncomeUpdateRequest incomeRequest,
            Authentication authentication) {
        try {
            Income updatedIncome = incomeService.update(id, incomeRequest);
            return ResponseEntity.ok(incomeMapper.toResponse(updatedIncome));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        try {
            Income income = incomeService.findById(id);
            incomeService.softDelete(income);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
