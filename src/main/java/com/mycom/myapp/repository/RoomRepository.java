package com.mycom.myapp.repository;

import com.mycom.myapp.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
