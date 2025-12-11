package com.mycom.myapp.service.room;

import com.mycom.myapp.dto.RoomDTO;

import java.util.List;

public interface RoomService {
    
    // 전체 룸 조회
    List<RoomDTO> getAllRooms();
    
    // 룸 생성 (관리자)
    RoomDTO createRoom(RoomDTO dto);
    
    // 이름으로 룸 활성화/비활성화 (관리자)
    RoomDTO toggleRoomAvailabilityByName(String name, Boolean isAvailable);
}
