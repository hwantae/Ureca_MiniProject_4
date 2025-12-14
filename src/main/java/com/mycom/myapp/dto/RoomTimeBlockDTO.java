package com.mycom.myapp.dto;

import com.mycom.myapp.domain.RoomTimeBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * RoomTimeBlock DTO (V5 신규)
 * - 예약 불가 구간 전송용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTimeBlockDTO {
    private Long id;
    private Long roomId;
    private LocalDate blockDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Entity -> DTO 변환
    public static RoomTimeBlockDTO from(RoomTimeBlock block) {
        return RoomTimeBlockDTO.builder()
                .id(block.getId())
                .roomId(block.getRoomId())
                .blockDate(block.getBlockDate())
                .startTime(block.getStartTime())
                .endTime(block.getEndTime())
                .build();
    }
}
