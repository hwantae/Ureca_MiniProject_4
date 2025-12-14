package com.mycom.myapp.repository;

import com.mycom.myapp.domain.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    // V5: 방 ID로 조회 (정렬 추가)
    List<TimeSlot> findByRoomIdOrderByStartTimeAsc(Long roomId);
    
    @Query("SELECT DISTINCT new com.mycom.myapp.dto.SlotConfigDTO(t.startTime, t.endTime) FROM TimeSlot t ORDER BY t.startTime")
    List<com.mycom.myapp.dto.SlotConfigDTO> findDistinctConfigs();

    void deleteByRoomId(Long roomId);
}
