package com.example.backend.repository;

import com.example.backend.constant.enums.AppointmentSlotStatus;
import com.example.backend.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    // Slot của 1 bác sĩ, trong ngày, còn AVAILABLE và chưa gán appointment
    List<AppointmentSlot> findByDoctorIdAndStartTimeBetweenAndStatusAndAppointmentIsNull(
            Long doctorId,
            LocalDateTime dayStart,
            LocalDateTime nextDayStart,
            AppointmentSlotStatus status
    );
}
