package com.mycom.myapp.dto;

import com.mycom.myapp.domain.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * TimeSlot DTO (V5 구조)
 * - slotDate 제거 → 시간 템플릿만
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {
    private Long slotId;
    private Long roomId;
    private LocalTime startTime;
    private LocalTime endTime;

    // Entity -> DTO 변환
    public static TimeSlotDTO from(TimeSlot slot) {
        return TimeSlotDTO.builder()
                .slotId(slot.getSlotId())
                .roomId(slot.getRoomId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .build();
    }
}
