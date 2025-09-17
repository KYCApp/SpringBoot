package com.mohamedMoslemani.kyc.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tessdata"); 
        tesseract.setLanguage("eng"); 
    }

    public String extractText(File imageFile) {
        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            throw new RuntimeException("OCR failed", e);
        }
    }
}
