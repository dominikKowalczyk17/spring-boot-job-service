package com.dominik.jobapplicationservice.jobs.service;

import com.dominik.jobapplicationservice.jobs.entity.JobOffer;
import com.dominik.jobapplicationservice.jobs.repository.JobsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    JobsRepository jobsRepository;
    public JobService(JobsRepository jobsRepository) {
        this.jobsRepository = jobsRepository;
    }

    public List<JobOffer>  getJobOffers(){
        return jobsRepository.findAll();
    }

    public JobOffer saveJobOffer(JobOffer jobOffer){
        return jobsRepository.save(jobOffer);
    }

    public boolean existsByUrl(String url){
        return getJobOffers().stream().anyMatch(jobOffer -> jobOffer.getUrl().equals(url));
    }

    public JobOffer getJobOfferById(Long id){
        return jobsRepository.findById(id).get();
    }

    public void deleteJobOfferById(Long id){
        jobsRepository.deleteById(id);
    }

    public JobOffer updateJobOffer(JobOffer jobOffer, Long id){
        Optional<JobOffer> optionalJobOffer = jobsRepository.findById(id);
        if(optionalJobOffer.isPresent()){
            JobOffer jobOfferToUpdate = optionalJobOffer.get();
            jobOfferToUpdate.setTitle(jobOffer.getTitle());
            jobOfferToUpdate.setCompany(jobOffer.getCompany());
            jobOfferToUpdate.setSalaryRange(jobOffer.getSalaryRange());
            jobOfferToUpdate.setUrl(jobOffer.getUrl());

            return jobsRepository.save(jobOfferToUpdate);
        }
        return null;
    }
}
