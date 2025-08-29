package com.dominik.jobapplicationservice.cv.repository;

import com.dominik.jobapplicationservice.cv.entity.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    Optional<CV> findByJobOfferIdIsNull();
    Optional<CV> findByJobOfferId(Long jobOfferId);
}
