package com.fraudshield.controller;

import com.fraudshield.dto.AuthRequest;
import com.fraudshield.dto.AuthResponse;
import com.fraudshield.dto.RegisterRequest;
import com.fraudshield.dto.UserDTO;
import com.fraudshield.entity.User;
import com.fraudshield.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        AuthResponse response = authService.register(request, clientIp);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                              HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        AuthResponse response = authService.login(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = authService.getCurrentAuthenticatedUser();
        UserDTO dto = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                0,
                user.getCreatedAt()
        );
        return ResponseEntity.ok(dto);
    }
}
