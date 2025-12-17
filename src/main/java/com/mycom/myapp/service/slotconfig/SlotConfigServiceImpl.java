package com.mycom.myapp.service.slotconfig;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import com.mycom.myapp.service.slotconfig.dto.SlotConfigRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlotConfigServiceImpl implements SlotConfigService {

    private final TimeSlotRepository timeSlotRepository;
    private final RoomRepository roomRepository;

    // ==========================
    // 운영시간(전체) 조회
    // ==========================
    @Override
    public List<SlotConfigRequest> getConfigs() {
        return timeSlotRepository.findDistinctConfigs().stream()
                .map(dto -> {
                    SlotConfigRequest req = new SlotConfigRequest();
                    req.setStartTime(dto.getStartTime());
                    req.setEndTime(dto.getEndTime());
                    return req;
                })
                .toList();
    }

    // ==========================
    // 운영시간(전체) 교체
    // - 어떤 형태로 오든(09~20 한 덩어리 포함) 무조건 1시간 슬롯으로 저장
    // ==========================
    @Override
    @Transactional
    public void replaceConfigs(List<SlotConfigRequest> configs) {

        if (configs == null || configs.isEmpty()) {
            throw new IllegalArgumentException("configs is empty");
        }

        // 1) null 방어 + 정렬
        List<SlotConfigRequest> sorted = configs.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SlotConfigRequest::getStartTime))
                .toList();

        // 2) 검증 + ✅ 1시간 단위로 정규화(분해)
        List<SlotConfigRequest> normalized = normalizeToHourly(sorted);

        // 3) 모든 룸 조회
        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) return;

        // 4) 기존 time_slot 전부 삭제
        for (Room r : rooms) {
            timeSlotRepository.deleteByRoomId(r.getRoomId());
            timeSlotRepository.flush();
        }

        // 5) 새 configs(= 1시간 단위로 분해된 목록) 기준으로 모든 룸에 재생성
        for (Room r : rooms) {
            Long roomId = r.getRoomId();
            for (SlotConfigRequest c : normalized) {
                TimeSlot slot = TimeSlot.builder()
                        .roomId(roomId)
                        .startTime(c.getStartTime())
                        .endTime(c.getEndTime())
                        .build();
                timeSlotRepository.save(slot);
            }
        }
    }

    /**
     * 입력 configs를 "무조건 1시간 단위 슬롯"으로 변환한다.
     * - 예: [09:00~20:00] -> [09~10, 10~11, ..., 19~20]
     * - 예: [09~12, 13~15] -> 각각 쪼개서 합침
     *
     * 제한:
     * - 시작/종료는 60분 단위(정각)여야 함
     * - 구간 길이는 60의 배수여야 함
     */
    private List<SlotConfigRequest> normalizeToHourly(List<SlotConfigRequest> input) {
        List<SlotConfigRequest> out = new ArrayList<>();

        for (SlotConfigRequest c : input) {
            LocalTime s = c.getStartTime();
            LocalTime e = c.getEndTime();

            if (s == null || e == null) {
                throw new IllegalArgumentException("startTime/endTime is null");
            }
            if (!s.isBefore(e)) {
                throw new IllegalArgumentException("startTime must be before endTime");
            }

            int sMin = s.getHour() * 60 + s.getMinute();
            int eMin = e.getHour() * 60 + e.getMinute();

            // 정각/1시간 단위 체크
            if (sMin % 60 != 0 || eMin % 60 != 0) {
                throw new IllegalArgumentException("운영시간은 정각(00분) 기준으로만 설정 가능합니다. 예) 09:00 ~ 20:00");
            }
            if ((eMin - sMin) % 60 != 0) {
                throw new IllegalArgumentException("운영시간은 1시간 단위로만 설정 가능합니다.");
            }

            // ✅ 1시간 단위로 분해
            for (int t = sMin; t < eMin; t += 60) {
                SlotConfigRequest one = new SlotConfigRequest();
                one.setStartTime(LocalTime.of(t / 60, 0));
                one.setEndTime(LocalTime.of((t + 60) / 60, 0));
                out.add(one);
            }
        }

        // 혹시 중복 제거(같은 시간대가 여러 번 들어온 경우)
        out = out.stream()
                .distinct()
                .sorted(Comparator.comparing(SlotConfigRequest::getStartTime))
                .toList();

        return out;
    }
}
