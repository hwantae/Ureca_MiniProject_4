package com.mycom.myapp.service.timeslot;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.dto.TimeSlotDTO;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final RoomRepository roomRepository;

    // 룸 이름 → roomId 변환
    private Long getRoomIdByName(String roomName) {
        Room room = roomRepository.findByName(roomName);
        if (room == null) {
            throw new RuntimeException("Room not found with name: " + roomName);
        }
        return room.getRoomId();
    }

    @Override
    public List<TimeSlotDTO> getSlotsByRoomNameAndDate(String roomName, LocalDate date) {
        Long roomId = getRoomIdByName(roomName);
        return timeSlotRepository.findByRoomIdAndSlotDate(roomId, date).stream()
                .map(TimeSlotDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getSlotsByRoomName(String roomName) {
        Long roomId = getRoomIdByName(roomName);
        return timeSlotRepository.findByRoomId(roomId).stream()
                .map(TimeSlotDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TimeSlotDTO createSlotByRoomName(String roomName, TimeSlotDTO dto) {
        Long roomId = getRoomIdByName(roomName);
        TimeSlot slot = TimeSlot.builder()
                .roomId(roomId)
                .slotDate(dto.getSlotDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .isAvailable(true)
                .build();
        TimeSlot savedSlot = timeSlotRepository.save(slot);
        return TimeSlotDTO.from(savedSlot);
    }

    @Override
    @Transactional
    public TimeSlotDTO toggleSlotAvailability(Long slotId, Boolean isAvailable) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found with id: " + slotId));
        slot.setIsAvailable(isAvailable);
        return TimeSlotDTO.from(slot);
    }
}
