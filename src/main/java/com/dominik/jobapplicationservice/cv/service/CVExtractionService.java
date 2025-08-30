package com.dominik.jobapplicationservice.cv.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class CVExtractionService {
    public String extractTextFromPDF(String filePath) throws IOException {
        File file = new File(filePath);
        try (PDDocument pdf = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }
}
