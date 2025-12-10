package com.mycom.myapp.service.reservation;

import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;

import java.util.List;

public interface ReservationService {

    Reservation createReservation(Long userId, CreateReservationRequest request);

    List<Reservation> getUserReservations(Long userId);

    void cancelReservation(Long userId, Long reservationId);
}
