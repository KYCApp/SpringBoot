package com.mohamedMoslemani.kyc.service;

import com.mohamedMoslemani.kyc.model.Customer;
import com.mohamedMoslemani.kyc.model.KycStatus;
import com.mohamedMoslemani.kyc.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    // --- Core CRUD ---
    public Customer saveCustomer(Customer customer) {
        validateCustomer(customer);
        return repo.save(customer);
    }

    public Optional<Customer> getCustomer(Long id) {
        return repo.findById(id);
    }

    public Optional<Customer> getCustomerForUser(Long id, String username) {
        return repo.findById(id)
                   .filter(c -> c.getUser().getEmail().equals(username));
    }

    public List<Customer> getAllCustomers() {
        return repo.findAll();
    }

    public void deleteCustomer(Long id) {
        repo.deleteById(id);
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return repo.findByEmail(email);
    }

    public Optional<Customer> getCustomerByIdNumber(String idNumber) {
        return repo.findByIdNumber(idNumber);
    }

    public void updateStatus(Long id, KycStatus status) {
        Customer customer = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setKycStatus(status);
        repo.save(customer);
    }

    // --- Validation ---
    private void validateCustomer(Customer customer) {
        // Age must be >= 18
        if (customer.getDateOfBirth() != null) {
            int age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new IllegalArgumentException("Customer must be at least 18 years old.");
            }
        }

        // ID expiry date must be in the future
        if (customer.getIdExpiryDate() != null) {
            if (!customer.getIdExpiryDate().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("ID expiry date must be in the future.");
            }
        }
    }
}
