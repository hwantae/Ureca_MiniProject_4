package com.mycom.myapp.service.reservation.dto;

import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.domain.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservationResponse {

    private Long reservationId;
    private Long userId;
    private Long roomId;
    private Long slotId;
    private ReservationStatus status;
    private LocalDateTime reservedAt;

    // ✅ V6 추가
    private LocalDate reservationDate;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUserId())
                .roomId(reservation.getRoomId())
                .slotId(reservation.getSlotId())
                .status(reservation.getStatus())
                .reservedAt(reservation.getReservedAt())
                .reservationDate(reservation.getReservationDate()) // ✅
                .build();
    }
}
