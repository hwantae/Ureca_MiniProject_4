package com.mycom.myapp.repository;

import com.mycom.myapp.domain.RoomTimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RoomTimeBlockRepository extends JpaRepository<RoomTimeBlock, Long> {

    // 특정 방의 특정 날짜 막힌 시간 조회
    List<RoomTimeBlock> findByRoomIdAndBlockDate(Long roomId, LocalDate blockDate);

    void deleteByRoomId(Long roomId);

    // 특정 방의 모든 막힌 시간 조회
    List<RoomTimeBlock> findByRoomId(Long roomId);
}
