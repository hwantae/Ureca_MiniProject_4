package com.mycom.myapp.service.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.repository.ReservationRepository;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        // 매 테스트 실행 전에 DB를 깨끗하게 정리
        reservationRepository.deleteAll();
        timeSlotRepository.deleteAll();
        roomRepository.deleteAll();

        // 1) 테스트용 Room 생성
        Room room = Room.builder()
                .name("동시성테스트룸")
                .capacity(4)
                .isAvailable(true)
                .maxUsageMinutes(120)
                .build();

        room = roomRepository.save(room); // 생성된 roomId 사용

        // 2) 테스트용 TimeSlot 생성 (내일 10:00 ~ 11:00)
        TimeSlot slot = TimeSlot.builder()
                .roomId(room.getRoomId())
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .isAvailable(true)
                .build();

        timeSlotRepository.save(slot);
    }

    @Test
    @DisplayName("동시에 여러 요청이 들어와도 같은 slotId에는 하나만 예약된다")
    void concurrentReservation() throws InterruptedException {

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 각 쓰레드의 성공/실패 결과를 저장할 리스트
        List<Boolean> results = new CopyOnWriteArrayList<>();

        // 테스트용 Room, TimeSlot 조회
        Room room = roomRepository.findAll().get(0);
        TimeSlot slot = timeSlotRepository.findAll().get(0);

        // 모든 쓰레드가 동일한 roomId, slotId에 대해 예약을 시도하게 설정
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRoomId(room.getRoomId());
        request.setSlotId(slot.getSlotId());

        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1L; // 각 쓰레드마다 다른 userId 사용

            executorService.submit(() -> {
                try {
                    reservationService.createReservation(userId, request);
                    results.add(true);  // 예약 성공
                } catch (Exception e) {
                    // 낙관적 락 충돌, ReservationException 등은 모두 실패로 처리
                    results.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 끝날 때까지 최대 10초 대기
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        long successCount = results.stream().filter(r -> r).count();
        long failureCount = results.stream().filter(r -> !r).count();

        // ✅ 검증 1: 성공한 예약은 정확히 1건이어야 한다
        assertEquals(1L, successCount);

        // ✅ 검증 2: 실패한 요청은 최소 1건 이상이어야 한다 (동시 요청이므로)
        // 필요 없으면 주석 처리해도 됨
//         assertTrue(failureCount >= 1);

        // ✅ 검증 3: 실제 DB에 저장된 예약 건수도 1이어야 한다
        long reservationCount = reservationRepository.count();
        assertEquals(1L, reservationCount);
    }
}
