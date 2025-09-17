package com.mohamedMoslemani.kyc.repository;

import com.mohamedMoslemani.kyc.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByCustomerId(Long customerId);
}
