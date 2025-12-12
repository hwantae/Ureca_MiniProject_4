package com.mycom.myapp.service.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.exception.ReservationException;
import com.mycom.myapp.service.reservation.ReservationValidator;

import static org.junit.jupiter.api.Assertions.*;

class ReservationValidatorTest {

    private final ReservationValidator validator = new ReservationValidator();

    private TimeSlot createSlot(LocalDate date, LocalTime start, LocalTime end) {
        return TimeSlot.builder()
                .slotId(1L)
                .roomId(1L)
                .slotDate(date)
                .startTime(start)
                .endTime(end)
                .isAvailable(true)
                .build();
    }

    private Room createRoom(int maxMinutes) {
        return Room.builder()
                .roomId(1L)
                .name("A Room")
                .capacity(4)
                .isAvailable(true)
                .maxUsageMinutes(maxMinutes)
                .build();
    }

    @Test
    @DisplayName("시간 범위 검증 성공(start < end)")
    void testValidateTimeSlotRangeSuccess() {
        TimeSlot slot = createSlot(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertDoesNotThrow(() -> validator.validateTimeSlotRange(slot));
    }

    @Test
    @DisplayName("시간 범위 검증 실패(start >= end)")
    void testValidateTimeSlotRangeFail() {
        TimeSlot slot = createSlot(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(10, 0));
        assertThrows(ReservationException.class, () -> validator.validateTimeSlotRange(slot));
    }

    @Test
    @DisplayName("과거 시간 예약 불가")
    void testValidateNotPastSlotFail() {
        TimeSlot slot = createSlot(LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertThrows(ReservationException.class, () -> validator.validateNotPastSlot(slot));
    }

    @Test
    @DisplayName("최대 이용시간 초과 → 실패")
    void testValidateMaxUsageFail() {
        Room room = createRoom(30);  // 최대 30분
        TimeSlot slot = createSlot(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0)); // 60분

        assertThrows(ReservationException.class, () -> validator.validateMaxUsage(slot, room));
    }

    @Test
    @DisplayName("취소 가능 시간 검증: 시작 30분 이내면 실패")
    void testValidateStartTimeForCancelFail() {
        LocalTime now = LocalTime.now();
        LocalTime start = now.plusMinutes(20);

        TimeSlot slot = createSlot(LocalDate.now(), start, start.plusMinutes(30));
        assertThrows(ReservationException.class, () -> validator.validateStartTimeForCancel(slot));
    }

    @Test
    @DisplayName("취소 가능 시간 검증: 30분 이상 남으면 성공")
    void testValidateStartTimeForCancelSuccess() {
        LocalTime now = LocalTime.now();
        LocalTime start = now.plusMinutes(40);

        TimeSlot slot = createSlot(LocalDate.now(), start, start.plusMinutes(60));
        assertDoesNotThrow(() -> validator.validateStartTimeForCancel(slot));
    }
}
