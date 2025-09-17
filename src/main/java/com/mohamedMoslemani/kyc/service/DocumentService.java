package com.mohamedMoslemani.kyc.service;

import com.mohamedMoslemani.kyc.model.*;
import com.mohamedMoslemani.kyc.repository.AuditLogRepository;
import com.mohamedMoslemani.kyc.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepo;
    private final AuditLogRepository auditRepo;

    public DocumentService(DocumentRepository documentRepo,
                           AuditLogRepository auditRepo) {
        this.documentRepo = documentRepo;
        this.auditRepo = auditRepo;
    }

    public Document saveDocument(Customer customer, MultipartFile file, String type, Long actorId, String action) throws IOException {
        Document doc = new Document();
        doc.setCustomer(customer);
        doc.setType(type);
        doc.setFileName(file.getOriginalFilename());
        doc.setContentType(file.getContentType());
        doc.setData(file.getBytes());

        Document saved = documentRepo.save(doc);

        // log the action
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setCustomerId(customer.getId());
        log.setAdminId(actorId); // could also be the same as customer.getUser().getId() for self-upload
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);

        return saved;
    }

    public Optional<Document> getDocument(Long id) {
        return documentRepo.findById(id);
    }
}
