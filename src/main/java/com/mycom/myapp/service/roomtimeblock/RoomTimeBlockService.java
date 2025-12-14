package com.mycom.myapp.service.roomtimeblock;

import com.mycom.myapp.dto.RoomTimeBlockDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * RoomTimeBlockService (V5 신규)
 * - 예약 불가 구간 관리
 */
public interface RoomTimeBlockService {
    
    // 특정 방의 특정 날짜 막힌 시간 조회
    List<RoomTimeBlockDTO> getBlocksByRoomAndDate(Long roomId, LocalDate date);
    
    // 특정 방의 모든 막힌 시간 조회
    List<RoomTimeBlockDTO> getBlocksByRoom(Long roomId);
    
    // 시간 막기 (생성)
    RoomTimeBlockDTO createBlock(RoomTimeBlockDTO dto);
    
    // 시간 열기 (삭제)
    void deleteBlock(Long id);
}
