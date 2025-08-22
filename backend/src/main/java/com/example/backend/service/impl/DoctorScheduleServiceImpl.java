package com.example.backend.service.impl;

import com.example.backend.constant.enums.AppointmentSlotStatus;
import com.example.backend.dto.DoctorScheduleCreateRequest;
import com.example.backend.dto.DoctorScheduleUpdateRequest;
import com.example.backend.dto.ScheduleSlotDto;
import com.example.backend.entity.AppointmentSlot;
import com.example.backend.entity.Doctor;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.AppointmentSlotRepository;
import com.example.backend.repository.DoctorRepository;
import com.example.backend.service.DoctorScheduleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorRepository doctorRepository;
    private final AppointmentSlotRepository slotRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ScheduleSlotDto> createSlots(Long doctorId, DoctorScheduleCreateRequest req) {
        log.info("Create schedule doctorId={}, start={}, end={}, interval={}", doctorId,
                req.startTime(), req.endTime(), req.intervalMinutes());

        validateWindow(req.startTime(), req.endTime());

        // Tìm bác sĩ
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ResourceNotFoundException("error.doctor.not.found", doctorId));

        // Tính interval (phút). Null => tạo 1 slot duy nhất bằng cả khoảng.
        int interval = (req.intervalMinutes() == null)
                ? Math.toIntExact(Duration.between(req.startTime(), req.endTime()).toMinutes())
                : req.intervalMinutes();

        if (interval <= 0) {
            throw new BusinessException("error.request.invalid",
                    "intervalMinutes must be positive");
        }

        List<AppointmentSlot> toSave = new ArrayList<>();
        LocalDateTime cur = req.startTime();

        // Chia slot liên tiếp [cur, next), slot cuối có thể ngắn hơn nếu không chia
        // hết.
        while (cur.isBefore(req.endTime())) {
            LocalDateTime next = cur.plusMinutes(interval);
            if (next.isAfter(req.endTime()))
                next = req.endTime();

            // Kiểm tra overlap với slot khác của cùng bác sĩ
            if (existsOverlap(doctorId, cur, next)) {
                throw new BusinessException("error.appointment.slot.unavailable",
                        "Overlaps with existing slot");
            }

            AppointmentSlot s = new AppointmentSlot();
            s.setDoctor(doctor);
            s.setStartTime(cur);
            s.setEndTime(next);
            s.setStatus(AppointmentSlotStatus.AVAILABLE);
            toSave.add(s);

            cur = next;
        }

        List<AppointmentSlot> saved = slotRepository.saveAll(toSave);
        return saved.stream().map(this::toDto).toList();
    }

    @Override
    public ScheduleSlotDto updateSlot(Long doctorId, Long slotId, DoctorScheduleUpdateRequest req) {
        log.info("Update schedule doctorId={}, slotId={}, start={}, end={}", doctorId, slotId,
                req.startTime(), req.endTime());

        validateWindow(req.startTime(), req.endTime());

        // Lấy slot thuộc về đúng doctor
        AppointmentSlot slot = slotRepository.findByIdAndDoctorId(slotId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("error.slot.not.found", slotId));

        // Chỉ cho sửa khi slot còn AVAILABLE
        if (slot.getStatus() != AppointmentSlotStatus.AVAILABLE) {
            throw new BusinessException("error.appointment.slot.unavailable",
                    "Cannot edit a non-AVAILABLE slot");
        }

        // Nếu thời gian không thay đổi, bỏ qua kiểm tra overlap
        boolean unchanged = req.startTime().equals(slot.getStartTime())
                && req.endTime().equals(slot.getEndTime());

        if (!unchanged
                && existsOverlapExcludingSelf(doctorId, slotId, req.startTime(), req.endTime())) {
            throw new BusinessException("error.appointment.slot.unavailable",
                    "Updated time overlaps with other slots");
        }

        slot.setStartTime(req.startTime());
        slot.setEndTime(req.endTime());
        AppointmentSlot saved = slotRepository.save(slot);
        return toDto(saved);
    }

    // ===================== helpers =====================

    private void validateWindow(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BusinessException("error.request.invalid", "startTime/endTime is null");
        }
        if (!end.isAfter(start)) {
            throw new BusinessException("error.request.invalid", "endTime must be after startTime");
        }
    }

    /** Kiểm tra có slot nào (cùng doctor) giao nhau [start, end) hay không */
    private boolean existsOverlap(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return slotRepository.existsOverlap(doctorId, start, end);
    }

    /**
     * Kiểm tra overlap nhưng loại trừ chính slot đang update. Dùng JPQL trực tiếp
     * để không cần thay đổi repository.
     */
    private boolean existsOverlapExcludingSelf(Long doctorId, Long slotId, LocalDateTime start,
            LocalDateTime end) {
        String jpql = """
                select count(s) from AppointmentSlot s
                where s.doctor.id = :doctorId
                  and s.id <> :slotId
                  and s.startTime < :end
                  and s.endTime   > :start
                """;
        TypedQuery<Long> q = em.createQuery(jpql, Long.class).setParameter("doctorId", doctorId)
                .setParameter("slotId", slotId).setParameter("start", start)
                .setParameter("end", end);
        Long cnt = q.getSingleResult();
        return cnt != null && cnt > 0;
    }

    private ScheduleSlotDto toDto(AppointmentSlot s) {
        return new ScheduleSlotDto(s.getId(), s.getStartTime(), s.getEndTime());
    }
}
