package com.mohamedMoslemani.kyc.controller;

import com.mohamedMoslemani.kyc.dto.CustomerRequestDTO;
import com.mohamedMoslemani.kyc.dto.CustomerResponseDTO;
import com.mohamedMoslemani.kyc.mapper.CustomerMapper;
import com.mohamedMoslemani.kyc.model.Customer;
import com.mohamedMoslemani.kyc.model.User;
import com.mohamedMoslemani.kyc.repository.DocumentRepository;
import com.mohamedMoslemani.kyc.repository.UserRepository;
import com.mohamedMoslemani.kyc.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mohamedMoslemani.kyc.model.Document;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;
    private final UserRepository userRepo;
    private final DocumentRepository documentRepo;

    public CustomerController(CustomerService service, UserRepository userRepo, DocumentRepository documentRepo) {
        this.service = service;
        this.userRepo = userRepo;
        this.documentRepo = documentRepo;
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Customer customer = CustomerMapper.toEntity(dto);
        customer.setUser(user); // link logged-in user to KYC record

        Customer saved = service.saveCustomer(customer);
        return ResponseEntity.ok(CustomerMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> get(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.getCustomerForUser(id, username)
                .map(c -> ResponseEntity.ok(CustomerMapper.toResponse(c)))
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody CustomerRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.getCustomerForUser(id, username)
                .map(existing -> {
                    existing.setFirstName(dto.getFirstName());
                    existing.setLastName(dto.getLastName());
                    existing.setDateOfBirth(dto.getDateOfBirth());
                    existing.setNationality(dto.getNationality());
                    existing.setGender(dto.getGender());
                    existing.setEmail(dto.getEmail());
                    existing.setPhoneNumber(dto.getPhoneNumber());
                    existing.setAddress(dto.getAddress());
                    existing.setCity(dto.getCity());
                    existing.setCountry(dto.getCountry());
                    existing.setIdNumber(dto.getIdNumber());
                    existing.setIdType(dto.getIdType());
                    existing.setIdExpiryDate(dto.getIdExpiryDate());

                    Customer updated = service.saveCustomer(existing);
                    return ResponseEntity.ok(CustomerMapper.toResponse(updated));
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.getCustomerForUser(id, username)
                .map(c -> {
                    service.deleteCustomer(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAll() {
        List<CustomerResponseDTO> customers = service.getAllCustomers()
                .stream()
                .map(CustomerMapper::toResponse)
                .toList();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    public ResponseEntity<CustomerResponseDTO> search(@RequestParam(required = false) String email,
                                                      @RequestParam(required = false) String idNumber) {
        Optional<Customer> customer = Optional.empty();

        if (email != null) {
            customer = service.getCustomerByEmail(email);
        } else if (idNumber != null) {
            customer = service.getCustomerByIdNumber(idNumber);
        }

        return customer.map(c -> ResponseEntity.ok(CustomerMapper.toResponse(c)))
                       .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{customerId}/upload")
public ResponseEntity<String> uploadDocument(
        @PathVariable Long customerId,
        @RequestParam("type") String type,
        @RequestParam("file") MultipartFile file) throws IOException {

    Customer customer = service.getCustomer(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    Document doc = new Document();
    doc.setCustomer(customer);
    doc.setType(type);
    doc.setFileName(file.getOriginalFilename());
    doc.setContentType(file.getContentType());
    doc.setData(file.getBytes());

    documentRepo.save(doc);
    return ResponseEntity.ok("Uploaded " + type);
}

}
