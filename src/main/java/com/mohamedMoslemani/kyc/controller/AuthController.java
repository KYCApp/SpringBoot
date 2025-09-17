package com.mohamedMoslemani.kyc.controller;

import com.mohamedMoslemani.kyc.dto.ChangePasswordRequest;
import com.mohamedMoslemani.kyc.dto.LoginRequest;
import com.mohamedMoslemani.kyc.dto.UserDTO;
import com.mohamedMoslemani.kyc.model.Role;
import com.mohamedMoslemani.kyc.model.User;
import com.mohamedMoslemani.kyc.model.VerificationToken;
import com.mohamedMoslemani.kyc.repository.UserRepository;
import com.mohamedMoslemani.kyc.repository.VerificationTokenRepository;
import com.mohamedMoslemani.kyc.security.JwtUtil;
import com.mohamedMoslemani.kyc.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final VerificationTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepo,
                          VerificationTokenRepository tokenRepo,
                          EmailService emailService,
                          PasswordEncoder encoder,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (userRepo.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setEnabled(false); // not active yet
        User saved = userRepo.save(user);

        // create token
        String token = UUID.randomUUID().toString();
        VerificationToken vtoken = new VerificationToken();
        vtoken.setToken(token);
        vtoken.setUser(saved);
        vtoken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepo.save(vtoken);

        // send email
        emailService.sendVerificationEmail(saved.getEmail(), token);

        return ResponseEntity.ok("Registered. Check your email to confirm.");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) {
        VerificationToken vtoken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (vtoken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        User user = vtoken.getUser();
        user.setEnabled(true);
        userRepo.save(user);
        tokenRepo.delete(vtoken);

        return ResponseEntity.ok("Account verified. You can now log in.");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Old password is incorrect");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepo.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }
}
