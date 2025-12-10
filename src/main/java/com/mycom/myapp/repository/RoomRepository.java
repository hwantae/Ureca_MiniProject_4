package com.mycom.myapp.repository;

import com.mycom.myapp.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    
    // 활성화된 룸만 조회
    List<Room> findByIsAvailableTrue();
    
    // 이름으로 룸 조회
    Room findByName(String name);
    
    // 수용 인원 이상인 룸 조회
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);
}
