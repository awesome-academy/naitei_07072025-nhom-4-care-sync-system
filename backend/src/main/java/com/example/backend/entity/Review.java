package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    @Column(name = "expertise_rating", nullable = false)
    private Integer expertiseRating;

    @Column(name = "communication_rating", nullable = false)
    private Integer communicationRating;

    @Column(name = "punctuality_rating", nullable = false)
    private Integer punctualityRating;

    @Column(name = "care_rating", nullable = false)
    private Integer careRating;

    @Column(name = "is_recommended", nullable = false)
    private Boolean isRecommended;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

    // Legacy field for backward compatibility
    @Column(name = "rating")
    private Integer rating;
}
