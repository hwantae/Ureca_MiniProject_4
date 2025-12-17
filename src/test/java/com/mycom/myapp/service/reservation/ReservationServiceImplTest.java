package com.mycom.myapp.service.reservation;

import com.mycom.myapp.domain.*;
import com.mycom.myapp.exception.ReservationException;
import com.mycom.myapp.repository.ReservationRepository;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.RoomTimeBlockRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ✅ 엔티티 builder/필드 구조가 달라도 깨지지 않게
 * Room/TimeSlot/Reservation을 전부 mock으로 처리한 서비스 유닛테스트
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock ReservationRepository reservationRepository;
    @Mock RoomRepository roomRepository;
    @Mock TimeSlotRepository timeSlotRepository;
    @Mock RoomTimeBlockRepository roomTimeBlockRepository;
    @Mock ReservationValidator validator;

    @InjectMocks ReservationServiceImpl reservationService;

    Room room;
    TimeSlot slot;

    @BeforeEach
    void setUp() {
        room = mock(Room.class);
        slot = mock(TimeSlot.class);

        when(room.getRoomId()).thenReturn(1L);
        when(room.getIsAvailable()).thenReturn(true);
        when(room.getMaxUsageMinutes()).thenReturn(180);

        when(slot.getSlotId()).thenReturn(3L);
        when(slot.getRoomId()).thenReturn(1L);
        when(slot.getStartTime()).thenReturn(LocalTime.of(9, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(10, 0));

        // validator는 void -> 기본 doNothing
        doNothing().when(validator).validateTimeSlotRange(any(TimeSlot.class));
        doNothing().when(validator).validateNotPastSlot(any(TimeSlot.class), any(LocalDate.class));
        doNothing().when(validator).validateMaxUsage(any(TimeSlot.class), any(Room.class));
        doNothing().when(validator).validateStartTimeForCancel(any(TimeSlot.class), any(LocalDate.class));
    }

    private CreateReservationRequest req(LocalDate date) {
        CreateReservationRequest r = new CreateReservationRequest();
        r.setRoomId(1L);
        r.setSlotId(3L);
        r.setStartTime(LocalDateTime.of(date, LocalTime.of(9, 0)));
        r.setEndTime(LocalDateTime.of(date, LocalTime.of(10, 0)));
        return r;
    }

    @Test
    @DisplayName("예약 생성 - 관리자 차단(block)된 시간이면 ReservationException")
    void createReservation_blocked_throws() {
        LocalDate date = LocalDate.of(2025, 12, 16);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(timeSlotRepository.findById(3L)).thenReturn(Optional.of(slot));

        when(roomTimeBlockRepository.existsByRoomIdAndBlockDateAndStartTime(
                eq(1L), eq(date), eq(LocalTime.of(9, 0))
        )).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(10L, req(date)))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("차단");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 생성 - 같은 slot/date에 CONFIRMED 예약 있으면 ReservationException")
    void createReservation_alreadyConfirmed_throws() {
        LocalDate date = LocalDate.of(2025, 12, 16);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(timeSlotRepository.findById(3L)).thenReturn(Optional.of(slot));

        when(roomTimeBlockRepository.existsByRoomIdAndBlockDateAndStartTime(anyLong(), any(), any()))
                .thenReturn(false);

        when(reservationRepository.countBySlotIdAndReservationDateAndStatus(
                3L, date, ReservationStatus.CONFIRMED
        )).thenReturn(1L);

        assertThatThrownBy(() -> reservationService.createReservation(10L, req(date)))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("이미");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 생성 - DB unique 충돌(DataIntegrityViolationException)을 ReservationException으로 변환")
    void createReservation_duplicateKey_translatesException() {
        LocalDate date = LocalDate.of(2025, 12, 16);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(timeSlotRepository.findById(3L)).thenReturn(Optional.of(slot));

        when(roomTimeBlockRepository.existsByRoomIdAndBlockDateAndStartTime(anyLong(), any(), any()))
                .thenReturn(false);

        when(reservationRepository.countBySlotIdAndReservationDateAndStatus(anyLong(), any(), any()))
                .thenReturn(0L);

        when(reservationRepository.save(any(Reservation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> reservationService.createReservation(10L, req(date)))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("이미");

        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 취소 - cancel 후 delete 처리(재예약 가능)")
    void cancelReservation_deletesReservation() {
        long userId = 10L;
        long reservationId = 55L;

        Reservation reservation = mock(Reservation.class);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        when(reservation.getSlotId()).thenReturn(3L);
        when(reservation.getReservationDate()).thenReturn(LocalDate.of(2025, 12, 16));

        when(timeSlotRepository.findById(3L)).thenReturn(Optional.of(slot));

        doNothing().when(reservation).validateOwner(userId);
        doNothing().when(reservation).cancel();

        reservationService.cancelReservation(userId, reservationId);

        verify(reservation, times(1)).validateOwner(userId);
        verify(reservation, times(1)).cancel();
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    @DisplayName("getReservedSlotIds - room/date CONFIRMED 예약의 slotId만 반환")
    void getReservedSlotIds_returnsConfirmedSlotIds() {
        LocalDate date = LocalDate.of(2025, 12, 16);

        Reservation r1 = mock(Reservation.class);
        when(r1.getSlotId()).thenReturn(3L);
        Reservation r2 = mock(Reservation.class);
        when(r2.getSlotId()).thenReturn(4L);

        when(reservationRepository.findByRoomIdAndReservationDateAndStatus(
                1L, date, ReservationStatus.CONFIRMED
        )).thenReturn(List.of(r1, r2));

        List<Long> ids = reservationService.getReservedSlotIds(1L, date);

        assertThat(ids).containsExactlyInAnyOrder(3L, 4L);
    }
}
