package com.mycom.myapp.controller;

import com.mycom.myapp.dto.RoomTimeBlockDTO;
import com.mycom.myapp.service.roomtimeblock.RoomTimeBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * RoomTimeBlockController (V5 신규)
 * - 예약 불가 구간 관리 API
 */
@RestController
@RequestMapping("/api/rooms/{roomId}/blocks")
@RequiredArgsConstructor
public class RoomTimeBlockController {

    private final RoomTimeBlockService roomTimeBlockService;

    // 특정 방의 특정 날짜 막힌 시간 조회
    @GetMapping
    public ResponseEntity<List<RoomTimeBlockDTO>> getBlocks(
            @PathVariable("roomId") Long roomId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date != null) {
            return ResponseEntity.ok(roomTimeBlockService.getBlocksByRoomAndDate(roomId, date));
        }
        return ResponseEntity.ok(roomTimeBlockService.getBlocksByRoom(roomId));
    }

    // 시간 막기 (생성)
    @PostMapping
    public ResponseEntity<RoomTimeBlockDTO> createBlock(
            @PathVariable("roomId") Long roomId,
            @RequestBody RoomTimeBlockDTO dto) {
        dto.setRoomId(roomId);
        RoomTimeBlockDTO created = roomTimeBlockService.createBlock(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 시간 열기 (삭제)
    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> deleteBlock(@PathVariable("blockId") Long blockId) {
        roomTimeBlockService.deleteBlock(blockId);
        return ResponseEntity.noContent().build();
    }
}
