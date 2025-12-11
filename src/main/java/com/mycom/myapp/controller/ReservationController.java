package com.mycom.myapp.controller;

import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.service.reservation.ReservationService;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;
import com.mycom.myapp.service.reservation.dto.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 1) 예약 생성
    @PostMapping
    public ReservationResponse createReservation(@RequestBody @Valid CreateReservationRequest request) {

        Long fakeUserId = 1L; // 🔥 테스트용 하드코딩
        Reservation reservation = reservationService.createReservation(fakeUserId, request);
        return ReservationResponse.from(reservation);
    }
//  @PostMapping
//  public ReservationResponse createReservation(
//          @AuthenticationPrincipal(expression = "userId") Long userId,
//          @RequestBody @Valid CreateReservationRequest request
//  ) {
//      Reservation reservation = reservationService.createReservation(userId, request);
//      return ReservationResponse.from(reservation);
//  }

    // 2) 내 예약 목록 조회
    @GetMapping("/me")
    public List<ReservationResponse> getMyReservations() {

        Long fakeUserId = 1L; // 🔥 테스트용 하드코딩
        List<Reservation> reservations = reservationService.getUserReservations(fakeUserId);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
//  @GetMapping("/me")
//  public List<ReservationResponse> getMyReservations(
//          @AuthenticationPrincipal(expression = "userId") Long userId
//  ) {
//      List<Reservation> reservations = reservationService.getUserReservations(userId);
//      return reservations.stream()
//              .map(ReservationResponse::from)
//              .toList();
//  }

    // 3) 예약 취소
    @DeleteMapping("/{reservationId}")
    public void cancelReservation(@PathVariable Long reservationId) {

        Long fakeUserId = 1L; // 🔥 테스트용 하드코딩
        reservationService.cancelReservation(fakeUserId, reservationId);
    }
//  @DeleteMapping("/{reservationId}")
//  public void cancelReservation(
//          @AuthenticationPrincipal(expression = "userId") Long userId,
//          @PathVariable Long reservationId
//  ) {
//      reservationService.cancelReservation(userId, reservationId);
//  }
  
}
