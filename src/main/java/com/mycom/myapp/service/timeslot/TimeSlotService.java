package com.mycom.myapp.service.timeslot;

import com.mycom.myapp.dto.TimeSlotDTO;

import java.util.List;

/**
 * TimeSlotService (V5 구조)
 * - 날짜 없이 시간 템플릿만 관리
 */
public interface TimeSlotService {
    
    // 룸 이름으로 시간 템플릿 조회
    List<TimeSlotDTO> getSlotsByRoomName(String roomName);
    
    // 모든 방에 동일한 시간 템플릿 생성
    List<TimeSlotDTO> createSlotForAllRooms(TimeSlotDTO dto);

    // V5: 가상 설정 조회 (모든 방의 공통 시간표)
    List<com.mycom.myapp.dto.SlotConfigDTO> getGlobalConfig();

    // V5: 가상 설정 업데이트 (모든 방의 시간표 일괄 변경)
    void updateGlobalConfig(List<com.mycom.myapp.dto.SlotConfigDTO> configs);
}
