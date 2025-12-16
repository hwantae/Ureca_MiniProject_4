package com.mycom.myapp.repository;

import com.mycom.myapp.domain.RoomTimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface RoomTimeBlockRepository extends JpaRepository<RoomTimeBlock, Long> {

    // 특정 방의 특정 날짜 막힌 시간 조회
    List<RoomTimeBlock> findByRoomIdAndBlockDate(Long roomId, LocalDate blockDate);

    // 특정 방의 모든 막힌 시간 조회
    List<RoomTimeBlock> findByRoomId(Long roomId);

    // 특정 방의 모든 막힌 시간 삭제
    void deleteByRoomId(Long roomId);

    // ✅ [중요] 예약 생성 시 차단 여부 확인용
    boolean existsByRoomIdAndBlockDateAndStartTime(
            Long roomId,
            LocalDate blockDate,
            LocalTime startTime
    );
}
