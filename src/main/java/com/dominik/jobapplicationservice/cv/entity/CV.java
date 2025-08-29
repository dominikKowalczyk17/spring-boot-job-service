package com.dominik.jobapplicationservice.cv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cvs")
@NoArgsConstructor
@AllArgsConstructor
public class CV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 500)
    String filePath;

    @Column(nullable = false, length = 500, unique = true)
    private String fileName;

    @Column(nullable = false, columnDefinition = "TEXT")
    String extractedText;

    @Column(name = "job_offer_id")
    Long jobOfferId;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
