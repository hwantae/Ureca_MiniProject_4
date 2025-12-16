package com.mycom.myapp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ReservationRequestDto {
    private Long roomId;
    private LocalDate date;
    private String startTime;
    private String endTime;
}
