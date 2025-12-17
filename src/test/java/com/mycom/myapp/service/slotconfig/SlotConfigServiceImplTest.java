package com.mycom.myapp.service.slotconfig;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycom.myapp.domain.Room;
import com.mycom.myapp.domain.TimeSlot;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import com.mycom.myapp.service.slotconfig.SlotConfigServiceImpl;
import com.mycom.myapp.service.slotconfig.dto.SlotConfigRequest;

/**
 * 운영시간(전체) 변경이 UI에 반영되려면:
 * - replaceConfigs()가 기존 슬롯 삭제 후
 * - 1시간 단위 슬롯으로 정상 재생성해야 함
 */
@ExtendWith(MockitoExtension.class)
class SlotConfigServiceImplTest {

    @Mock TimeSlotRepository timeSlotRepository;
    @Mock RoomRepository roomRepository;

    @InjectMocks SlotConfigServiceImpl slotConfigService;

    @Test
    @DisplayName("replaceConfigs - 09:00~12:00 입력 시 09~10,10~11,11~12로 1시간 슬롯 생성 + 모든 룸에 재생성")
    void replaceConfigs_normalizesToHourly_andRecreatesAllRooms() {
        Room r1 = Room.builder().roomId(1L).build();
        Room r2 = Room.builder().roomId(2L).build();
        when(roomRepository.findAll()).thenReturn(List.of(r1, r2));

        SlotConfigRequest cfg = new SlotConfigRequest();
        cfg.setStartTime(LocalTime.of(9, 0));
        cfg.setEndTime(LocalTime.of(12, 0));

        slotConfigService.replaceConfigs(List.of(cfg));

        // 기존 삭제: 룸마다 deleteByRoomId 호출
        verify(timeSlotRepository, times(1)).deleteByRoomId(1L);
        verify(timeSlotRepository, times(1)).deleteByRoomId(2L);

        // 생성: 룸 2개 * (09~12 => 3슬롯) = save 6번
        verify(timeSlotRepository, times(6)).save(any(TimeSlot.class));
    }

}
