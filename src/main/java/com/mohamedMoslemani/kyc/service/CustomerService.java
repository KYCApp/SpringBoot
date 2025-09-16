package com.mohamedMoslemani.kyc.service;

import com.mohamedMoslemani.kyc.model.Customer;
import com.mohamedMoslemani.kyc.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public Customer saveCustomer(Customer c) {
        return repo.save(c);
    }

    public Optional<Customer> getCustomer(Long id) {
        return repo.findById(id);
    }

    public void deleteCustomer(Long id) {
        repo.deleteById(id);
    }

    public List<Customer> getAllCustomers() {
    return repo.findAll();
}
public Optional<Customer> getCustomerByEmail(String email) {
    return repo.findByEmail(email);
}

public Optional<Customer> getCustomerByIdNumber(String idNumber) {
    return repo.findByIdNumber(idNumber);
}


    
}
