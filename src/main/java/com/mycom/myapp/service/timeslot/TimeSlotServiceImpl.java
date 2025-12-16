package com.mycom.myapp.service.timeslot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.dto.SlotConfigDTO;
import com.mycom.myapp.dto.TimeSlotDTO;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;

import lombok.RequiredArgsConstructor;

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
        // ✅ 기존: config 1개당 슬롯 1개 생성 → (09:00~22:00) 하나만 생김
        // ✅ 변경: (start~end) 범위를 1시간 단위로 쪼개서 여러 슬롯 생성
        for (Room room : allRooms) {
            for (com.mycom.myapp.dto.SlotConfigDTO config : configs) {

                java.time.LocalTime start = config.getStartTime();
                java.time.LocalTime end = config.getEndTime();

                // 1시간 단위 생성
                java.time.LocalTime cur = start;
                while (cur.isBefore(end)) {
                    java.time.LocalTime next = cur.plusHours(1);
                    if (next.isAfter(end)) break; // 끝이 딱 떨어지지 않으면 마지막은 생성 안함(정책)

                    TimeSlot slot = TimeSlot.builder()
                            .roomId(room.getRoomId())
                            .startTime(cur)
                            .endTime(next)
                            .build();
                    timeSlotRepository.save(slot);

                    cur = next;
                }
            }
        }
    }

}
