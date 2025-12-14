package com.mycom.myapp.controller;

import com.mycom.myapp.dto.TimeSlotDTO;
import com.mycom.myapp.service.timeslot.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TimeSlotController (V5 구조)
 * - 시간 템플릿만 관리 (날짜 없음)
 */
@RestController
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    // 해당 방 시간 템플릿 조회
    @GetMapping("/api/rooms/name/{roomName}/slots")
    public ResponseEntity<List<TimeSlotDTO>> getSlotsByRoomName(
            @PathVariable("roomName") String roomName) {
        return ResponseEntity.ok(timeSlotService.getSlotsByRoomName(roomName));
    }

    // 모든 방에 동일한 시간 템플릿 생성
    @PostMapping("/api/slots")
    public ResponseEntity<List<TimeSlotDTO>> createSlotForAllRooms(@RequestBody TimeSlotDTO dto) {
        List<TimeSlotDTO> created = timeSlotService.createSlotForAllRooms(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // V5: 가상 설정 조회
    @GetMapping("/api/slots/config")
    public java.util.List<com.mycom.myapp.dto.SlotConfigDTO> getGlobalConfig() {
        return timeSlotService.getGlobalConfig();
    }

    // V5: 가상 설정 업데이트
    @PutMapping("/api/slots/config")
    public void updateGlobalConfig(@RequestBody java.util.List<com.mycom.myapp.dto.SlotConfigDTO> configs) {
        timeSlotService.updateGlobalConfig(configs);
    }
}
