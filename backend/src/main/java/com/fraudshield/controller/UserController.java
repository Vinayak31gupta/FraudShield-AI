package com.fraudshield.controller;

import com.fraudshield.dto.UserDTO;
import com.fraudshield.entity.User;
import com.fraudshield.repository.TransactionRepository;
import com.fraudshield.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final AuthService authService;
    private final TransactionRepository transactionRepository;

    public UserController(AuthService authService, TransactionRepository transactionRepository) {
        this.authService = authService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile() {
        User user = authService.getCurrentAuthenticatedUser();
        long txnCount = transactionRepository.countByUser(user);

        UserDTO dto = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                txnCount,
                user.getCreatedAt()
        );

        return ResponseEntity.ok(dto);
    }
}
