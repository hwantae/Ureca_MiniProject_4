package com.mycom.myapp.service.reservation;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.exception.ReservationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class ReservationValidator {

    // 슬롯의 시간 범위 자체가 이상한지(시작 >= 종료) 검사
    public void validateTimeSlotRange(TimeSlot timeSlot) {
        LocalTime start = timeSlot.getStartTime();
        LocalTime end = timeSlot.getEndTime();
        if (!start.isBefore(end)) {
            throw new ReservationException("타임슬롯의 시작/종료 시간이 올바르지 않습니다.");
        }
    }

    // 과거 시간 슬롯인지 검사
    public void validateNotPastSlot(TimeSlot timeSlot) {
        LocalDateTime slotStartDateTime =
                LocalDateTime.of(timeSlot.getSlotDate(), timeSlot.getStartTime());

        if (slotStartDateTime.isBefore(LocalDateTime.now())) {
            throw new ReservationException("과거 시간대는 예약할 수 없습니다.");
        }
    }

    // 룸의 최대 이용시간보다 긴 슬롯인지 검사
    public void validateMaxUsage(TimeSlot timeSlot, Room room) {
        long minutes = Duration.between(
                timeSlot.getStartTime(),
                timeSlot.getEndTime()
        ).toMinutes();

        if (minutes > room.getMaxUsageMinutes()) {
            throw new ReservationException("최대 이용 시간을 초과한 타임슬롯입니다.");
        }
    }

    // 취소 가능 시간(시작 30분 전까지)
    public void validateStartTimeForCancel(TimeSlot timeSlot) {
        LocalDateTime slotStartDateTime =
                LocalDateTime.of(timeSlot.getSlotDate(), timeSlot.getStartTime());

        if (LocalDateTime.now().isAfter(slotStartDateTime.minusMinutes(30))) {
            throw new ReservationException("시작 30분 전 이후에는 취소할 수 없습니다.");
        }
    }
}
