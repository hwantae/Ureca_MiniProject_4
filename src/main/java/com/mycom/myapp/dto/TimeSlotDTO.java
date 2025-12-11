package com.mycom.myapp.dto;

import com.mycom.myapp.domain.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {
    private Long slotId;
    private Long roomId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;

    // Entity -> DTO 변환
    public static TimeSlotDTO from(TimeSlot slot) {
        return TimeSlotDTO.builder()
                .slotId(slot.getSlotId())
                .roomId(slot.getRoomId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.getIsAvailable())
                .build();
    }
}
