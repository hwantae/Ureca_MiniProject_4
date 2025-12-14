package com.mycom.myapp.service.room;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.dto.RoomDTO;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
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
    private final TimeSlotRepository timeSlotRepository;
    private final com.mycom.myapp.repository.RoomTimeBlockRepository roomTimeBlockRepository;

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
                .isAvailable(true)
                .maxUsageMinutes(120)
                .build();
        Room savedRoom = roomRepository.save(room);

        List<Room> existingRooms = roomRepository.findAll();
        if (existingRooms.size() > 1) {
            Room otherRoom = existingRooms.stream()
                    .filter(r -> !r.getRoomId().equals(savedRoom.getRoomId()))
                    .findFirst()
                    .orElse(null);

            if (otherRoom != null) {
                List<TimeSlot> templates = timeSlotRepository.findByRoomIdOrderByStartTimeAsc(otherRoom.getRoomId());
                for (TimeSlot template : templates) {
                    TimeSlot newSlot = TimeSlot.builder()
                            .roomId(savedRoom.getRoomId())
                            .startTime(template.getStartTime())
                            .endTime(template.getEndTime())
                            .build();
                    timeSlotRepository.save(newSlot);
                }
            }
        }
        return RoomDTO.from(savedRoom);
    }

    @Override
    @Transactional
    public RoomDTO toggleRoomAvailabilityByName(String name, Boolean isAvailable) {
        Room room = roomRepository.findByName(name);
        if (room == null) throw new RuntimeException("Room not found: " + name);
        room.setIsAvailable(isAvailable);
        return RoomDTO.from(room);
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        room.setName(dto.getName());
        room.setCapacity(dto.getCapacity());
        return RoomDTO.from(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        // 수동 연관 삭제 (Cascade)
        roomTimeBlockRepository.deleteByRoomId(id);
        timeSlotRepository.deleteByRoomId(id);
        roomRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateAllRoomsMaxUsage(Integer minutes) {
        List<Room> allRooms = roomRepository.findAll();
        for (Room room : allRooms) {
            room.setMaxUsageMinutes(minutes);
        }
    }
}
