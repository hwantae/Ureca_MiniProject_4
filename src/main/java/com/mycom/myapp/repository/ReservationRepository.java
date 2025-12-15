package com.mycom.myapp.repository;

import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 같은 타임슬롯에 이미 확정된 예약이 있는지 체크
    long countBySlotIdAndStatus(Long slotId, ReservationStatus status);

    // 같은 타임슬롯, 같은 날짜에 이미 확정된 예약이 있는지 체크 (V6 추가)
    long countBySlotIdAndReservationDateAndStatus(Long slotId, java.time.LocalDate reservationDate, ReservationStatus status);

    // 특정 유저의 예약 목록
    java.util.List<Reservation> findByUserId(Long userId);
}
