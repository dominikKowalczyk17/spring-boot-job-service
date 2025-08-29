package com.dominik.jobapplicationservice.jobs.controller;

import com.dominik.jobapplicationservice.jobs.entity.JobOffer;
import com.dominik.jobapplicationservice.jobs.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("api/v1/jobs")
public class JobsController {
    JobService jobService;

    public JobsController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<List<JobOffer>> getAllJobOffers(){
        jobService.getJobOffers();
        return ResponseEntity.ok().body(jobService.getJobOffers());
    }

    @PostMapping()
    public ResponseEntity<?> saveJobOffer(@RequestBody JobOffer jobOffer) {
        if (jobService.existsByUrl(jobOffer.getUrl())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Job offer with '" +jobOffer.getUrl() + "' already exists");
        }
        JobOffer saved = jobService.saveJobOffer(jobOffer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobOffer> updateJobOffer(@RequestBody JobOffer jobOffer,
                                                   @PathVariable Long id) {
        JobOffer updatedJob = jobService.updateJobOffer(jobOffer, id);
        return ResponseEntity.status(HttpStatus.OK).body(updatedJob);
    }
}
