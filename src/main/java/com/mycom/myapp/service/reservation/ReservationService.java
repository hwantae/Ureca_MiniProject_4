package com.mycom.myapp.service.reservation;

import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    Reservation createReservation(Long userId, CreateReservationRequest request);

    List<Reservation> getUserReservations(Long userId);

    void cancelReservation(Long userId, Long reservationId);

    // ✅ [추가] 특정 룸/날짜에 이미 예약된 slotId 목록 (UI 예약됨 표시용)
    List<Long> getReservedSlotIds(Long roomId, LocalDate date);

    // ✅ [추가] 특정 룸/날짜의 예약 목록 (rooms.html "예약 n건" 배지 용)
    List<Reservation> getRoomReservations(Long roomId, LocalDate date);
}
