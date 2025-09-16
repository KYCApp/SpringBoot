package com.mohamedMoslemani.kyc.mapper;

import com.mohamedMoslemani.kyc.dto.CustomerRequestDTO;
import com.mohamedMoslemani.kyc.dto.CustomerResponseDTO;
import com.mohamedMoslemani.kyc.model.Customer;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequestDTO dto) {
        Customer c = new Customer();
        c.setFirstName(dto.getFirstName());
        c.setLastName(dto.getLastName());
        c.setDateOfBirth(dto.getDateOfBirth());
        c.setNationality(dto.getNationality());
        c.setGender(dto.getGender());
        c.setEmail(dto.getEmail());
        c.setPhoneNumber(dto.getPhoneNumber());
        c.setAddress(dto.getAddress());
        c.setCity(dto.getCity());
        c.setCountry(dto.getCountry());
        c.setIdNumber(dto.getIdNumber());
        c.setIdType(dto.getIdType());
        c.setIdExpiryDate(dto.getIdExpiryDate());
        return c;
    }

    public static CustomerResponseDTO toResponse(Customer c) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(c.getId());
        dto.setFirstName(c.getFirstName());
        dto.setLastName(c.getLastName());
        dto.setDateOfBirth(c.getDateOfBirth());
        dto.setNationality(c.getNationality());
        dto.setGender(c.getGender());
        dto.setEmail(c.getEmail());
        dto.setPhoneNumber(c.getPhoneNumber());
        dto.setAddress(c.getAddress());
        dto.setCity(c.getCity());
        dto.setCountry(c.getCountry());
        dto.setIdNumber(c.getIdNumber());
        dto.setIdType(c.getIdType());
        dto.setIdExpiryDate(c.getIdExpiryDate());
        dto.setKycStatus(c.getKycStatus());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    public static List<CustomerResponseDTO> toResponseList(List<Customer> customers) {
        return customers.stream()
                .map(CustomerMapper::toResponse)
                .collect(Collectors.toList());
    }
}
