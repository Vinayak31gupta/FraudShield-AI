package com.fraudshield.controller;

import com.fraudshield.dto.DashboardStatsDTO;
import com.fraudshield.dto.TransactionRequestDTO;
import com.fraudshield.dto.TransactionResponseDTO;
import com.fraudshield.entity.Transaction;
import com.fraudshield.entity.User;
import com.fraudshield.service.AuthService;
import com.fraudshield.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO,
            HttpServletRequest request) {

        User currentUser = authService.getCurrentAuthenticatedUser();
        String clientIp = request.getRemoteAddr();

        Transaction txn = transactionService.createTransaction(requestDTO, currentUser, clientIp);
        return new ResponseEntity<>(transactionService.mapToResponseDTO(txn), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponseDTO>> getUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User currentUser = authService.getCurrentAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponseDTO> transactions = transactionService.getUserTransactions(currentUser, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        TransactionResponseDTO dto = transactionService.getTransactionById(id, currentUser);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        DashboardStatsDTO stats = transactionService.getUserDashboardStats(currentUser);
        return ResponseEntity.ok(stats);
    }
}
