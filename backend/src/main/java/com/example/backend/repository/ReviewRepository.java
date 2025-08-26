package com.example.backend.repository;

import com.example.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find review by appointment ID
     */
    Optional<Review> findByAppointmentId(Long appointmentId);

    /**
     * Check if review exists for appointment
     */
    boolean existsByAppointmentId(Long appointmentId);

    /**
     * Find all reviews by doctor ID
     */
    @Query("""
            SELECT r FROM Review r
            JOIN r.appointment a
            JOIN a.appointmentSlot s
            WHERE s.doctor.id = :doctorId
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * Find all reviews by patient ID
     */
    @Query("""
            SELECT r FROM Review r
            JOIN r.appointment a
            WHERE a.patient.id = :patientId
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByPatientId(@Param("patientId") Long patientId);

    /**
     * Find all reviews by doctor ID with pagination
     */
    @Query("""
            SELECT r FROM Review r
            JOIN r.appointment a
            JOIN a.appointmentSlot s
            WHERE s.doctor.id = :doctorId
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByDoctorIdWithPagination(@Param("doctorId") Long doctorId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Count reviews by doctor ID
     */
    @Query("""
            SELECT COUNT(r) FROM Review r
            JOIN r.appointment a
            JOIN a.appointmentSlot s
            WHERE s.doctor.id = :doctorId
            """)
    long countByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * Count recommended reviews by doctor ID
     */
    @Query("""
            SELECT COUNT(r) FROM Review r
            JOIN r.appointment a
            JOIN a.appointmentSlot s
            WHERE s.doctor.id = :doctorId AND r.isRecommended = true
            """)
    long countRecommendedByDoctorId(@Param("doctorId") Long doctorId);
}
