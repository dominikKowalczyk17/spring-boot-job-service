package com.dominik.jobapplicationservice.cv.controller;


import com.dominik.jobapplicationservice.cv.entity.CV;
import com.dominik.jobapplicationservice.cv.service.CVExtractionService;
import com.dominik.jobapplicationservice.cv.service.CVService;
import com.dominik.jobapplicationservice.cv.service.CVTailoringService;
import com.dominik.jobapplicationservice.cv.service.CVTextPatternService;
import com.dominik.jobapplicationservice.jobs.entity.JobOffer;
import com.dominik.jobapplicationservice.jobs.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cv")
public class CVController {

    private CVService cvService;
    private CVExtractionService cvExtractionService;
    private CVTextPatternService cvTextPatternService;
    private CVTailoringService cvTailoringService;
    private JobService jobService;
    
    public CVController(CVService cvService, CVExtractionService cvExtractionService, 
                       CVTextPatternService cvTextPatternService, CVTailoringService cvTailoringService,
                       JobService jobService) {
        this.cvService = cvService;
        this.cvExtractionService = cvExtractionService;
        this.cvTextPatternService = cvTextPatternService;
        this.cvTailoringService = cvTailoringService;
        this.jobService = jobService;
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

    @GetMapping("/{id}/sections")
    public ResponseEntity<Map<String, CVTextPatternService.CVSectionContent>> extractCVSections(@PathVariable Long id) {
        try {
            CV cv = cvService.findById(id);
            if (cv == null) {
                return ResponseEntity.notFound().build();
            }
            
            String fullText = cvExtractionService.extractTextFromPDF(cv.getFilePath());

            Map<String, CVTextPatternService.CVSectionContent> sections = cvTextPatternService.extractCVSectionsByPattern(fullText);
            return ResponseEntity.ok(sections);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @PostMapping("/{cvId}/tailor/{jobId}")
    public ResponseEntity<CVTailoringService.TailoredContent> tailorCVForJob(@PathVariable Long cvId, @PathVariable Long jobId) {
        try {
            CV originalCV = cvService.findById(cvId);
            if (originalCV == null) {
                return ResponseEntity.notFound().build();
            }
            
            JobOffer jobOffer = jobService.getJobOfferById(jobId);
            if (jobOffer == null) {
                return ResponseEntity.notFound().build();
            }
            
            CVTailoringService.TailoredContent tailoredContent = cvTailoringService.tailorCVForJob(originalCV, jobOffer);
            return ResponseEntity.ok(tailoredContent);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
