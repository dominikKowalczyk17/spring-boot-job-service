package com.dominik.jobapplicationservice.jobs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "job_offers")
@NoArgsConstructor
@AllArgsConstructor
public class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(nullable = false)
    String salaryRange;

    @Column(nullable = false)
    String company;

    @Column(unique = true, nullable = false, length = 1000)
    String url;

    @Column(nullable = false)
    Boolean applied;
}
