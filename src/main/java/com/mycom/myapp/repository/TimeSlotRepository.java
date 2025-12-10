package com.mycom.myapp.repository;

import com.mycom.myapp.domain.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    // 특정 룸의 모든 슬롯 조회
    List<TimeSlot> findByRoomId(Long roomId);
    
    // 특정 룸의 특정 날짜 슬롯 조회
    List<TimeSlot> findByRoomIdAndSlotDate(Long roomId, LocalDate slotDate);
    
    // 특정 룸의 특정 날짜의 예약 가능한 슬롯 조회
    List<TimeSlot> findByRoomIdAndSlotDateAndIsAvailable(
            Long roomId, LocalDate slotDate, Boolean isAvailable);
}
