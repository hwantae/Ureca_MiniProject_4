package com.mycom.myapp.service.reservation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.domain.ReservationStatus;
import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.exception.ReservationException;

class ReservationTest {

    private Room room() {
        return Room.builder()
                .roomId(1L)
                .name("A Room")
                .capacity(4)
                .isAvailable(true)
                .maxUsageMinutes(120)
                .build();
    }

    private TimeSlot timeSlot() {
        return TimeSlot.builder()
                .slotId(100L)
                .roomId(1L)
                .slotDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .isAvailable(true)
                .build();
    }

    @Test
    @DisplayName("Reservation.create() 정상 생성")
    void testCreateReservation() {
        Reservation reservation = Reservation.create(1L, room(), timeSlot());

        assertEquals(1L, reservation.getUserId());
        assertEquals(1L, reservation.getRoomId());
        assertEquals(100L, reservation.getSlotId());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertNotNull(reservation.getReservedAt());
    }

    @Test
    @DisplayName("validateOwner() 실패 - 다른 유저")
    void testValidateOwnerFail() {
        Reservation reservation = Reservation.create(1L, room(), timeSlot());

        assertThrows(ReservationException.class,
                () -> reservation.validateOwner(999L));
    }

    @Test
    @DisplayName("validateOwner() 성공")
    void testValidateOwnerSuccess() {
        Reservation reservation = Reservation.create(1L, room(), timeSlot());

        assertDoesNotThrow(() -> reservation.validateOwner(1L));
    }

    @Test
    @DisplayName("cancel() 성공")
    void testCancelSuccess() {
        Reservation reservation = Reservation.create(1L, room(), timeSlot());

        reservation.cancel();

        assertEquals(ReservationStatus.CANCELED, reservation.getStatus());
    }

    @Test
    @DisplayName("cancel() 실패 - 이미 취소된 예약 재취소")
    void testCancelFail() {
        Reservation reservation = Reservation.create(1L, room(), timeSlot());
        reservation.cancel(); // 첫 취소

        assertThrows(ReservationException.class, reservation::cancel);
    }
}
