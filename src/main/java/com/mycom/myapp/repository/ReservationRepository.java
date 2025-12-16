package com.mycom.myapp.repository;

import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    long countBySlotIdAndStatus(Long slotId, ReservationStatus status);

    long countBySlotIdAndReservationDateAndStatus(Long slotId, java.time.LocalDate reservationDate, ReservationStatus status);

    java.util.List<Reservation> findByUserId(Long userId);

    // ✅ 추가: 내 예약 정렬 조회
    java.util.List<Reservation> findByUserIdOrderByReservationDateDesc(Long userId);
    
    java.util.List<com.mycom.myapp.domain.Reservation> 
    findByRoomIdAndReservationDateAndStatus(Long roomId, java.time.LocalDate reservationDate, com.mycom.myapp.domain.ReservationStatus status);

}
