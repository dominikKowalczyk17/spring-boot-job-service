package com.dominik.jobapplicationservice.jobs.repository;

import com.dominik.jobapplicationservice.jobs.entity.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobsRepository extends JpaRepository<JobOffer, Long> {

}
