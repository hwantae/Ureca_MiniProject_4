package com.mycom.myapp.service.timeslot;

import com.mycom.myapp.dto.TimeSlotDTO;

import java.time.LocalDate;
import java.util.List;

public interface TimeSlotService {
    
    // 룸 이름 + 날짜 슬롯 조회 (사용자용)
    List<TimeSlotDTO> getSlotsByRoomNameAndDate(String roomName, LocalDate date);
    
    // 룸 이름으로 전체 슬롯 조회 (관리자용)
    List<TimeSlotDTO> getSlotsByRoomName(String roomName);
    
    // 룸 이름으로 슬롯 생성 (관리자용)
    TimeSlotDTO createSlotByRoomName(String roomName, TimeSlotDTO dto);
    
    // 슬롯 활성화/비활성화 (관리자용)
    TimeSlotDTO toggleSlotAvailability(Long slotId, Boolean isAvailable);
}
