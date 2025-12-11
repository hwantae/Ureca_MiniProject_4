package com.mycom.myapp.controller;

import com.mycom.myapp.dto.TimeSlotDTO;
import com.mycom.myapp.service.timeslot.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    // ===== 사용자용 API =====

    // 날짜별 시간 블록 조회 (상세 페이지 - 달력 선택)
    @GetMapping("/api/rooms/name/{roomName}/slots/date")
    public ResponseEntity<List<TimeSlotDTO>> getSlotsByRoomNameAndDate(
            @PathVariable String roomName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(timeSlotService.getSlotsByRoomNameAndDate(roomName, date));
    }

    // ===== 관리자용 API =====

    // 해당 방 전체 시간 블록 조회
    @GetMapping("/api/rooms/name/{roomName}/slots")
    public ResponseEntity<List<TimeSlotDTO>> getSlotsByRoomName(@PathVariable String roomName) {
        return ResponseEntity.ok(timeSlotService.getSlotsByRoomName(roomName));
    }

    // 시간 블록 생성
    @PostMapping("/api/rooms/name/{roomName}/slots")
    public ResponseEntity<TimeSlotDTO> createSlotByRoomName(
            @PathVariable String roomName,
            @RequestBody TimeSlotDTO dto) {
        TimeSlotDTO created = timeSlotService.createSlotByRoomName(roomName, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 시간 블록 활성화/비활성화 (청소, 점검 등)
    @PatchMapping("/api/slots/{slotId}/availability")
    public ResponseEntity<TimeSlotDTO> toggleSlotAvailability(
            @PathVariable Long slotId,
            @RequestParam Boolean isAvailable) {
        return ResponseEntity.ok(timeSlotService.toggleSlotAvailability(slotId, isAvailable));
    }
}
