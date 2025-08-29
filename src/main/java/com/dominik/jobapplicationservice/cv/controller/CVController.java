package com.dominik.jobapplicationservice.cv.controller;


import com.dominik.jobapplicationservice.cv.entity.CV;
import com.dominik.jobapplicationservice.cv.service.CVExtractionService;
import com.dominik.jobapplicationservice.cv.service.CVService;
import com.dominik.jobapplicationservice.jobs.entity.JobOffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cv")
public class CVController {

    private CVService cvService;
    private CVExtractionService cvExtractionService;
    
    public CVController(CVService cvService, CVExtractionService cvExtractionService) {
        this.cvService = cvService;
        this.cvExtractionService = cvExtractionService;
    }

    @GetMapping
    public ResponseEntity<List<CV>> getCVs(){
        return ResponseEntity.ok(cvService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CV> getCVById(@PathVariable Long id) {
        CV cv = cvService.findById(id);
        if (cv != null)
            return ResponseEntity.ok(cv);
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CV> addCv(@RequestBody CV cv){
        if (cvService.existsByName(cv)) {
            return ResponseEntity.badRequest().body(cv);
        }
        return ResponseEntity.ok(cvService.save(cv));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CV> updateCv(@RequestBody CV cv, @PathVariable Long id) {
        CV updatedCv = cvService.updateCv(cv, id);
        return ResponseEntity.status(HttpStatus.OK).body(updatedCv);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testExtraction() {
        try {
            String extractedText = cvExtractionService.extractTextFromPDF(
                    "/home/dominik/git/JobApplicationService/uploads/cv/Dominik_Kowalczyk_Software_Engineer.pdf");
            return ResponseEntity.ok(extractedText);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
