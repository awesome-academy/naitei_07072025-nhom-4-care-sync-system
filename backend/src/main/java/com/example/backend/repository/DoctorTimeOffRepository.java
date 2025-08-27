package com.example.backend.repository;

import com.example.backend.entity.DoctorTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DoctorTimeOffRepository extends JpaRepository<DoctorTimeOff, Long> {

    @Query("""
            SELECT COUNT(dto) > 0 FROM DoctorTimeOff dto
            WHERE dto.doctor.id = :doctorId
            AND dto.startDatetime < :endDatetime
            AND dto.endDatetime > :startDatetime
            """)
    boolean existsOverlappingTimeOff(@Param("doctorId") Long doctorId,
            @Param("startDatetime") LocalDateTime startDatetime,
            @Param("endDatetime") LocalDateTime endDatetime);

    @Query("""
            SELECT COUNT(dto) > 0 FROM DoctorTimeOff dto
            WHERE dto.doctor.id = :doctorId AND dto.id <> :excludeId AND
            (dto.startDatetime < :endDateTime AND dto.endDatetime > :startDateTime)
            """)
    boolean existsOverlappingTimeOffExcludingId(@Param("doctorId") Long doctorId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime, @Param("excludeId") Long excludeId);
}
