package com.mohamedMoslemani.kyc.controller;

import com.mohamedMoslemani.kyc.dto.CustomerResponseDTO;
import com.mohamedMoslemani.kyc.dto.UserDTO;
import com.mohamedMoslemani.kyc.mapper.CustomerMapper;
import com.mohamedMoslemani.kyc.model.AuditLog;
import com.mohamedMoslemani.kyc.model.KycStatus;
import com.mohamedMoslemani.kyc.repository.AuditLogRepository;
import com.mohamedMoslemani.kyc.repository.CustomerRepository;
import com.mohamedMoslemani.kyc.repository.DocumentRepository;
import com.mohamedMoslemani.kyc.repository.UserRepository;
import com.mohamedMoslemani.kyc.service.CustomerService;
import com.mohamedMoslemani.kyc.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CustomerService service;
    private final UserRepository userRepo;
    private final AuditLogRepository auditLogRepo;
    private final EmailService emailService;
    private final DocumentRepository documentRepo;
    private final CustomerRepository customerRepo;

    public AdminController(CustomerService service,
                           UserRepository userRepo,
                           AuditLogRepository auditLogRepo,
                           EmailService emailService,
                           DocumentRepository documentRepo,
                           CustomerRepository customerRepo) {
        this.service = service;
        this.userRepo = userRepo;
        this.auditLogRepo = auditLogRepo;
        this.emailService = emailService;
        this.documentRepo = documentRepo;
        this.customerRepo = customerRepo;
    }

    // Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(Map.of("message", "Welcome Admin"));
    }

    // Stats placeholder (can be extended later)
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(Map.of("status", "all good"));
    }

    // Paginated users
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> users = userRepo.findAll(pageable)
                .map(u -> new UserDTO(u.getId(), u.getEmail(), u.getRole()));
        return ResponseEntity.ok(users);
    }

    // Update KYC status
    @PutMapping("/customers/{id}/status")
    public ResponseEntity<String> updateKycStatus(@PathVariable Long id,
                                                  @RequestParam("status") String status) {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Long adminId = userRepo.findByEmail(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"))
                .getId();

        return service.getCustomer(id)
                .map(customer -> {
                    try {
                        KycStatus newStatus = Enum.valueOf(KycStatus.class, status.toUpperCase());

                        customer.setKycStatus(newStatus);
                        service.saveCustomer(customer);

                        emailService.sendKycUpdate(customer.getEmail(), newStatus.name());

                        AuditLog log = new AuditLog();
                        log.setCustomerId(customer.getId());
                        log.setAdminId(adminId);
                        log.setAction(newStatus.name());
                        auditLogRepo.save(log);

                        return ResponseEntity.ok("KYC status updated to " + newStatus.name());
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity
                                .badRequest()
                                .body("Invalid status. Use PENDING, VERIFIED, or REJECTED.");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Pending customers
    @GetMapping("/customers/pending")
    public ResponseEntity<List<CustomerResponseDTO>> getPendingCustomers() {
        List<CustomerResponseDTO> pending = service.getAllCustomers().stream()
                .filter(c -> c.getKycStatus() == KycStatus.PENDING)
                .map(CustomerMapper::toResponse)
                .toList();
        return ResponseEntity.ok(pending);
    }

    // Customer by ID
    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        return service.getCustomer(id)
                .map(c -> ResponseEntity.ok(CustomerMapper.toResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Audit logs
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getLogs() {
        return ResponseEntity.ok(auditLogRepo.findAll());
    }

    // System-wide metrics
    @GetMapping("/metrics")
    public ResponseEntity<?> metrics() {
        return ResponseEntity.ok(Map.of(
                "users", userRepo.count(),
                "customers", customerRepo.count(),
                "documents", documentRepo.count()
        ));
    }
}
