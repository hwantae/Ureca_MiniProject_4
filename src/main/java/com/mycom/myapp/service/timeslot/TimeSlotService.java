package com.mycom.myapp.service.timeslot;

import com.mycom.myapp.dto.TimeSlotDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimeSlotService {
    
    // 룸 이름 + 날짜 슬롯 조회 (사용자용)
    List<TimeSlotDTO> getSlotsByRoomNameAndDate(String roomName, LocalDate date);
    
    // 룸 이름으로 전체 슬롯 조회 (관리자용)
    List<TimeSlotDTO> getSlotsByRoomName(String roomName);
    
    // 모든 방에 동일한 슬롯 생성 (관리자용)
    List<TimeSlotDTO> createSlotForAllRooms(TimeSlotDTO dto);
    
    // 슬롯 활성화/비활성화 - slotId 기반 (관리자용)
    TimeSlotDTO toggleSlotAvailability(Long slotId, Boolean isAvailable);
    
    // 슬롯 활성화/비활성화 - 방이름+시간 기반 (관리자용)
    TimeSlotDTO toggleSlotAvailabilityByRoomAndTime(String roomName, LocalDate date, LocalTime startTime, Boolean isAvailable);
}
