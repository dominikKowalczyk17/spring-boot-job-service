package com.dominik.jobapplicationservice.cv.service;


import com.dominik.jobapplicationservice.cv.entity.CV;
import com.dominik.jobapplicationservice.cv.repository.CVRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CVService {
    private CVRepository cvRepository;

    public CVService(CVRepository cvRepository) {
        this.cvRepository = cvRepository;
    }

    public List<CV> findAll() {
        return cvRepository.findAll();
    }

    public CV findById(Long id) {
        return cvRepository.findById(id).orElse(null);
    }

    public CV save(CV cv){
        return cvRepository.save(cv);
    }

    public boolean existsByName(CV cv){
        return findAll().stream().anyMatch(cvStream -> cvStream.getFileName().equals(cv.getFileName()));
    }

    public CV updateCv(CV cv, Long id){
        CV updatedCV = cvRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
        
        updatedCV.setFilePath(cv.getFilePath());
        updatedCV.setFileName(cv.getFileName());
        updatedCV.setExtractedText(cv.getExtractedText());
        updatedCV.setJobOfferId(cv.getJobOfferId());
        return cvRepository.save(updatedCV);
    }
    
    public CV findOriginalCV() {
        return cvRepository.findByJobOfferIdIsNull().orElse(null);
    }
    
    public CV findByJobId(Long jobId) {
        return cvRepository.findByJobOfferId(jobId).orElse(null);
    }
}
