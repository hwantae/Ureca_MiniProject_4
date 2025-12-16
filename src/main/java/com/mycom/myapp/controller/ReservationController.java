package com.mycom.myapp.controller;

import com.mycom.myapp.config.MyUserDetails;
import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.service.reservation.ReservationService;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;
import com.mycom.myapp.service.reservation.dto.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    // ==========================
    // 예약 생성
    // ==========================
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody CreateReservationRequest request
    ) {
        // ✅ 테스트용 하드코딩 제거
        Long userId = userDetails.getId();

        Reservation created = reservationService.createReservation(userId, request);
        return ResponseEntity.ok(ReservationResponse.from(created));
    }

    // ==========================
    // 내 예약 조회 (옵션: date, roomId 필터)
    // ==========================
    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "roomId", required = false) Long roomId
    ) {
        Long userId = userDetails.getId();

        List<ReservationResponse> list = reservationService.getUserReservations(userId).stream()
                // date 필터 (옵션)
                .filter(r -> date == null || date.equals(r.getReservationDate()))
                // roomId 필터 (옵션)
                .filter(r -> roomId == null || roomId.equals(r.getRoomId()))
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(list);
    }

    // ==========================
    // [B-Role] 룸별 예약 목록 조회 (rooms.html "예약 n건" 배지 용)
    // - 특정 roomId의 특정 날짜 예약 목록을 내려준다.
    // - 프론트에서 length로 건수 표시
    // ==========================
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<ReservationResponse>> getRoomReservationsByDate(
            @PathVariable("roomId") Long roomId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ReservationResponse> list = reservationService.getRoomReservations(roomId, date).stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(list);
    }

    // ==========================
    // [B-Role] 룸/날짜별 "예약된 slotId 목록" 조회 (room.html 예약불가 마킹 용)
    // - room.html에서 reserved 슬롯을 회색 처리하기 위해 List<Long> slotIds 내려줌
    // ==========================
    @GetMapping("/rooms/{roomId}/reserved-slot-ids")
    public ResponseEntity<List<Long>> getReservedSlotIds(
            @PathVariable("roomId") Long roomId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<Long> slotIds = reservationService.getReservedSlotIds(roomId, date);
        return ResponseEntity.ok(slotIds);
    }

    // ==========================
    // 예약 취소
    // ==========================
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(
            @AuthenticationPrincipal MyUserDetails userDetails,
            // ✅ 여기 때문에 500이 났던 케이스가 많음 (이름 명시!)
            @PathVariable("reservationId") Long reservationId
    ) {
        Long userId = userDetails.getId();
        reservationService.cancelReservation(userId, reservationId);
        return ResponseEntity.noContent().build();
    }
}
