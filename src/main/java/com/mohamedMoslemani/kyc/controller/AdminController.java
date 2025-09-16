package com.mohamedMoslemani.kyc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("Welcome, Admin. This is your dashboard.");
    }

    @GetMapping("/stats")
    public ResponseEntity<String> stats() {
        return ResponseEntity.ok("System stats: all good.");
    }
}
