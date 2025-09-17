package com.mohamedMoslemani.kyc.controller;

import com.mohamedMoslemani.kyc.model.Customer;
import com.mohamedMoslemani.kyc.model.Document;
import com.mohamedMoslemani.kyc.model.KycStatus;
import com.mohamedMoslemani.kyc.repository.CustomerRepository;
import com.mohamedMoslemani.kyc.service.CustomerService;
import com.mohamedMoslemani.kyc.service.DocumentService;
import com.mohamedMoslemani.kyc.service.OcrService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final CustomerRepository customerRepo;
    private final CustomerService customerService;
    private final DocumentService documentService;
    private final OcrService ocrService;

    public DocumentController(CustomerRepository customerRepo,
                              CustomerService customerService,
                              DocumentService documentService,
                              OcrService ocrService) {
        this.customerRepo = customerRepo;
        this.customerService = customerService;
        this.documentService = documentService;
        this.ocrService = ocrService;
    }

    @PostMapping("/upload/{customerId}")
    public ResponseEntity<?> upload(@PathVariable Long customerId,
                                    @RequestParam("type") String type,
                                    @RequestParam("file") MultipartFile file) throws IOException {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Customer> customerOpt = customerRepo.findById(customerId);

        if (customerOpt.isEmpty() || !customerOpt.get().getUser().getEmail().equals(username)) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Not allowed"));
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "File is empty"));
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "File size exceeds 5MB"));
        }

        Document saved = documentService.saveDocument(customerOpt.get(), file, type,
                customerOpt.get().getUser().getId(), "UPLOAD_DOC");

        return ResponseEntity.ok(Map.of("status", "success", "documentId", saved.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> download(@PathVariable Long id) {
        return documentService.getDocument(id)
                .map(doc -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                        .contentType(MediaType.parseMediaType(doc.getContentType()))
                        .body(doc.getData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{customerId}/upload-validate")
    public ResponseEntity<?> uploadAndValidate(@PathVariable Long customerId,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam("type") String type) throws IOException {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerService.getCustomer(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getUser().getEmail().equals(username)) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Not allowed"));
        }

        Document saved = documentService.saveDocument(customer, file, type,
                customer.getUser().getId(), "VALIDATE_DOC");

        if (type.equalsIgnoreCase("ID") || type.equalsIgnoreCase("PASSPORT")) {
            File temp = File.createTempFile("ocr-", ".png");
            file.transferTo(temp);

            String extracted = ocrService.extractText(temp);

            if (!extracted.contains(customer.getIdNumber())) {
                customer.setKycStatus(KycStatus.PENDING);
                customerService.saveCustomer(customer);
            }
            temp.delete();
        }

        return ResponseEntity.ok(Map.of("status", "success", "documentId", saved.getId(), "message", "File uploaded & validated"));
    }
}
