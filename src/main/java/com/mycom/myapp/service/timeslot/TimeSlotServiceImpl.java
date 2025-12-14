package com.mycom.myapp.service.timeslot;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.dto.TimeSlotDTO;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TimeSlotServiceImpl (V5 구조)
 * - 날짜 없이 시간 템플릿만 관리
 */
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
    public List<TimeSlotDTO> getSlotsByRoomName(String roomName) {
        Long roomId = getRoomIdByName(roomName);
        return timeSlotRepository.findByRoomIdOrderByStartTimeAsc(roomId).stream()
                .map(TimeSlotDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TimeSlotDTO> createSlotForAllRooms(TimeSlotDTO dto) {
        List<Room> allRooms = roomRepository.findAll();
        List<TimeSlotDTO> createdSlots = new ArrayList<>();
        
        for (Room room : allRooms) {
            TimeSlot slot = TimeSlot.builder()
                    .roomId(room.getRoomId())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .build();
            TimeSlot savedSlot = timeSlotRepository.save(slot);
            createdSlots.add(TimeSlotDTO.from(savedSlot));
        }
        
        return createdSlots;
    }

    @Override
    public List<com.mycom.myapp.dto.SlotConfigDTO> getGlobalConfig() {
        return timeSlotRepository.findDistinctConfigs();
    }

    @Override
    @Transactional
    public void updateGlobalConfig(List<com.mycom.myapp.dto.SlotConfigDTO> configs) {
        // 1. 기존 모든 슬롯 삭제 (주의: 실제 운영 환경에서는 예약된 슬롯 처리 필요)
        // 현재는 단순하게 모두 삭제 후 재생성 (B담당 예약 데이터와 무결성 주의)
        timeSlotRepository.deleteAll();

        // 2. 모든 방 조회
        List<Room> allRooms = roomRepository.findAll();

        // 3. 각 방마다 새 설정대로 슬롯 생성
        for (Room room : allRooms) {
            for (com.mycom.myapp.dto.SlotConfigDTO config : configs) {
                TimeSlot slot = TimeSlot.builder()
                        .roomId(room.getRoomId())
                        .startTime(config.getStartTime())
                        .endTime(config.getEndTime())
                        .build();
                timeSlotRepository.save(slot);
            }
        }
    }
}
