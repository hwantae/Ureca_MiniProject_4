package com.mycom.myapp.service.room;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.dto.RoomDTO;

import java.util.List;

/**
 * RoomService 통합 테스트 (C-Role)
 */
@SpringBootTest
@Transactional
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Test
    @DisplayName("전체 룸 조회")
    void testGetAllRooms() {
        List<RoomDTO> rooms = roomService.getAllRooms();
        assertNotNull(rooms);
    }

    @Test
    @DisplayName("룸 생성")
    void testCreateRoom() {
        RoomDTO dto = RoomDTO.builder().name("테스트룸").capacity(4).build();
        RoomDTO result = roomService.createRoom(dto);
        
        assertNotNull(result);
        assertEquals("테스트룸", result.getName());
    }

    @Test
    @DisplayName("룸 수정")
    void testUpdateRoom() {
        // 먼저 생성
        RoomDTO dto = RoomDTO.builder().name("수정전룸").capacity(4).build();
        RoomDTO created = roomService.createRoom(dto);
        
        // 수정
        RoomDTO updateDto = RoomDTO.builder().name("수정후룸").capacity(8).build();
        RoomDTO updated = roomService.updateRoom(created.getRoomId(), updateDto);
        
        assertEquals("수정후룸", updated.getName());
        assertEquals(8, updated.getCapacity());
    }
}
