package com.mycom.myapp.service.reservation.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateReservationRequest {
    private Long roomId;
    private Long slotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
