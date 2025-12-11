package com.mycom.myapp.service.room;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.dto.RoomDTO;
import com.mycom.myapp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomDTO createRoom(RoomDTO dto) {
        Room room = Room.builder()
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .isAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true)
                .maxUsageMinutes(dto.getMaxUsageMinutes() != null ? dto.getMaxUsageMinutes() : 120)
                .build();
        Room savedRoom = roomRepository.save(room);
        return RoomDTO.from(savedRoom);
    }

    @Override
    @Transactional
    public RoomDTO toggleRoomAvailabilityByName(String name, Boolean isAvailable) {
        Room room = roomRepository.findByName(name);
        if (room == null) {
            throw new RuntimeException("Room not found with name: " + name);
        }
        
        // TODO: 비활성화 시 미래 예약 취소 연동 (B담당)
        
        room.setIsAvailable(isAvailable);
        return RoomDTO.from(room);
    }
}
