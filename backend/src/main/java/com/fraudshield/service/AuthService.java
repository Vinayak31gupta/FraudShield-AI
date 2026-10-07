package com.fraudshield.service;

import com.fraudshield.dto.AuthRequest;
import com.fraudshield.dto.AuthResponse;
import com.fraudshield.dto.RegisterRequest;
import com.fraudshield.entity.Role;
import com.fraudshield.entity.User;
import com.fraudshield.exception.BadRequestException;
import com.fraudshield.exception.ResourceNotFoundException;
import com.fraudshield.repository.UserRepository;
import com.fraudshield.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("An account with this email address already exists.");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_USER;

        User user = new User(
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                role
        );

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        auditLogService.log(
                savedUser.getId(),
                savedUser.getEmail(),
                "USER_REGISTERED",
                "AUTH",
                ipAddress,
                "New account registered with role " + savedUser.getRole().name()
        );

        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name(),
                jwtUtil.getExpirationMs()
        );
    }

    @Transactional
    public AuthResponse login(AuthRequest request, String ipAddress) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        auditLogService.log(
                user.getId(),
                user.getEmail(),
                "USER_LOGIN_SUCCESS",
                "AUTH",
                ipAddress,
                "User successfully logged in via credentials"
        );

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                jwtUtil.getExpirationMs()
        );
    }

    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadRequestException("No authenticated user found in session context.");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }
}
