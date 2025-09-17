package com.mohamedMoslemani.kyc.controller;

import com.mohamedMoslemani.kyc.repository.DocumentRepository;
import com.mohamedMoslemani.kyc.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/documents")
public class AdminDocumentController {

    private final DocumentRepository documentRepo;
    private final CustomerRepository customerRepo;

    public AdminDocumentController(DocumentRepository documentRepo,
                                   CustomerRepository customerRepo) {
        this.documentRepo = documentRepo;
        this.customerRepo = customerRepo;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<DocumentMetadata>> listDocuments(@PathVariable Long customerId) {
        List<DocumentMetadata> docs = documentRepo.findByCustomerId(customerId).stream()
                .map(d -> new DocumentMetadata(
                        d.getId(),
                        d.getType(),
                        d.getFileName(),
                        d.getContentType()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(docs);
    }

    // Global system stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
                "totalCustomers", customerRepo.count(),
                "totalDocuments", documentRepo.count()
        ));
    }

    // Document metadata DTO
    private record DocumentMetadata(Long id, String type, String fileName, String contentType) {}
}
