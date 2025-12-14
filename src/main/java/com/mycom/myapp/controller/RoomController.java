package com.mycom.myapp.controller;

import com.mycom.myapp.dto.RoomDTO;
import com.mycom.myapp.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // ===== 사용자용 API =====

    // 전체 룸 조회 (메인 페이지)
    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    // ===== 관리자용 API =====

    // 룸 생성
    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO dto) {
        RoomDTO created = roomService.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 룸 활성화/비활성화 (공사, 청소 등)
    @PatchMapping("/name/{name}/availability")
    public ResponseEntity<RoomDTO> toggleRoomAvailabilityByName(
            @PathVariable("name") String name,
            @RequestParam("isAvailable") Boolean isAvailable) {
        return ResponseEntity.ok(roomService.toggleRoomAvailabilityByName(name, isAvailable));
    }

    // 룸 수정
    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable("id") Long id, @RequestBody RoomDTO dto) {
        return ResponseEntity.ok(roomService.updateRoom(id, dto));
    }

    // 룸 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // 전체 룸 최대 이용 시간 변경
    @PutMapping("/config/max-usage")
    public ResponseEntity<Void> updateMaxUsage(@RequestBody java.util.Map<String, Integer> body) {
        Integer minutes = body.get("minutes");
        roomService.updateAllRoomsMaxUsage(minutes);
        return ResponseEntity.ok().build();
    }
}
