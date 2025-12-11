package com.mycom.myapp.repository;

import com.mycom.myapp.domain.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByRoomIdAndSlotDateAndIsAvailable(
            Long roomId, LocalDate slotDate, Boolean isAvailable);
}
