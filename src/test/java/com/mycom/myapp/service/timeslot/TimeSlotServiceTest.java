package com.mycom.myapp.service.timeslot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.dto.RoomDTO;
import com.mycom.myapp.dto.TimeSlotDTO;
import com.mycom.myapp.service.room.RoomService;

import java.util.List;

/**
 * TimeSlotService 통합 테스트 (C-Role)
 */
@SpringBootTest
@Transactional
class TimeSlotServiceTest {

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private RoomService roomService;

    @Test
    @DisplayName("룸 이름으로 슬롯 조회")
    void testGetSlotsByRoomName() {
        // 먼저 룸 생성
        RoomDTO room = roomService.createRoom(
            RoomDTO.builder().name("슬롯테스트룸").capacity(4).build()
        );
        
        // 슬롯 조회 (없어도 빈 리스트 반환)
        List<TimeSlotDTO> slots = timeSlotService.getSlotsByRoomName("슬롯테스트룸");
        assertNotNull(slots);
    }

    @Test
    @DisplayName("없는 룸 조회 시 예외")
    void testGetSlotsByRoomNameNotFound() {
        assertThrows(RuntimeException.class, () -> 
            timeSlotService.getSlotsByRoomName("존재하지않는룸")
        );
    }
}
