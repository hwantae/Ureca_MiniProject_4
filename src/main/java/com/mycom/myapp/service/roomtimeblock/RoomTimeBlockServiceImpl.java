package com.mycom.myapp.service.roomtimeblock;

import com.mycom.myapp.domain.RoomTimeBlock;
import com.mycom.myapp.dto.RoomTimeBlockDTO;
import com.mycom.myapp.repository.RoomTimeBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RoomTimeBlockServiceImpl (V5 신규)
 * - 예약 불가 구간 관리 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTimeBlockServiceImpl implements RoomTimeBlockService {

    private final RoomTimeBlockRepository roomTimeBlockRepository;

    @Override
    public List<RoomTimeBlockDTO> getBlocksByRoomAndDate(Long roomId, LocalDate date) {
        return roomTimeBlockRepository.findByRoomIdAndBlockDate(roomId, date).stream()
                .map(RoomTimeBlockDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomTimeBlockDTO> getBlocksByRoom(Long roomId) {
        return roomTimeBlockRepository.findByRoomId(roomId).stream()
                .map(RoomTimeBlockDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomTimeBlockDTO createBlock(RoomTimeBlockDTO dto) {
        RoomTimeBlock block = RoomTimeBlock.builder()
                .roomId(dto.getRoomId())
                .blockDate(dto.getBlockDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
        RoomTimeBlock savedBlock = roomTimeBlockRepository.save(block);
        return RoomTimeBlockDTO.from(savedBlock);
    }

    @Override
    @Transactional
    public void deleteBlock(Long id) {
        roomTimeBlockRepository.deleteById(id);
    }
}
