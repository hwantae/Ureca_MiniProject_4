package com.mycom.myapp.dto;

import com.mycom.myapp.domain.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private Long roomId;
    private String name;
    private Integer capacity;
    private Boolean isAvailable;
    private Integer maxUsageMinutes;

    // Entity -> DTO 변환
    public static RoomDTO from(Room room) {
        return RoomDTO.builder()
                .roomId(room.getRoomId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .isAvailable(room.getIsAvailable())
                .maxUsageMinutes(room.getMaxUsageMinutes())
                .build();
    }
}
