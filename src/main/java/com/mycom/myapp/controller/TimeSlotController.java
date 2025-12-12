package com.mycom.myapp.controller;

import com.mycom.myapp.dto.TimeSlotDTO;
import com.mycom.myapp.service.timeslot.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    // ===== 사용자용 API =====

    // 날짜별 시간 블록 조회 (상세 페이지 - 달력 선택)
    @GetMapping("/api/rooms/name/{roomName}/slots/date")
    public ResponseEntity<List<TimeSlotDTO>> getSlotsByRoomNameAndDate(
            @PathVariable("roomName") String roomName,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(timeSlotService.getSlotsByRoomNameAndDate(roomName, date));
    }

    // ===== 관리자용 API =====

    // 해당 방 전체 시간 블록 조회
    @GetMapping("/api/rooms/name/{roomName}/slots")
    public ResponseEntity<List<TimeSlotDTO>> getSlotsByRoomName(@PathVariable("roomName") String roomName) {
        return ResponseEntity.ok(timeSlotService.getSlotsByRoomName(roomName));
    }

    // 모든 방에 동일한 시간 블록 생성
    @PostMapping("/api/slots")
    public ResponseEntity<List<TimeSlotDTO>> createSlotForAllRooms(@RequestBody TimeSlotDTO dto) {
        List<TimeSlotDTO> created = timeSlotService.createSlotForAllRooms(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 시간 블록 활성화/비활성화 - slotId 기반
    @PatchMapping("/api/slots/{slotId}/availability")
    public ResponseEntity<TimeSlotDTO> toggleSlotAvailability(
            @PathVariable("slotId") Long slotId,
            @RequestParam("isAvailable") Boolean isAvailable) {
        return ResponseEntity.ok(timeSlotService.toggleSlotAvailability(slotId, isAvailable));
    }

    // 시간 블록 활성화/비활성화 - 방이름+날짜+시간 기반
    @PatchMapping("/api/rooms/name/{roomName}/slots/availability")
    public ResponseEntity<TimeSlotDTO> toggleSlotAvailabilityByRoomAndTime(
            @PathVariable("roomName") String roomName,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam("isAvailable") Boolean isAvailable) {
        return ResponseEntity.ok(timeSlotService.toggleSlotAvailabilityByRoomAndTime(roomName, date, startTime, isAvailable));
    }
}
