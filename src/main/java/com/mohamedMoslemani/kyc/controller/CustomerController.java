package com.mohamedMoslemani.kyc.controller;

import com.mohamedMoslemani.kyc.dto.CustomerRequestDTO;
import com.mohamedMoslemani.kyc.dto.CustomerResponseDTO;
import com.mohamedMoslemani.kyc.mapper.CustomerMapper;
import com.mohamedMoslemani.kyc.model.Customer;
import com.mohamedMoslemani.kyc.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    // Create
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO dto) {
        Customer saved = service.saveCustomer(CustomerMapper.toEntity(dto));
        return ResponseEntity.ok(CustomerMapper.toResponse(saved));
    }

    // Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> get(@PathVariable Long id) {
        Optional<Customer> customer = service.getCustomer(id);
        return customer.map(c -> ResponseEntity.ok(CustomerMapper.toResponse(c)))
                       .orElse(ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody CustomerRequestDTO dto) {
        return service.getCustomer(id)
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
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.getCustomer(id)
                .map(c -> {
                    service.deleteCustomer(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // List all
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAll() {
        List<CustomerResponseDTO> customers = service.getAllCustomers()
                .stream()
                .map(CustomerMapper::toResponse)
                .toList();
        return ResponseEntity.ok(customers);
    }

    // Search by email or ID number
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
}
