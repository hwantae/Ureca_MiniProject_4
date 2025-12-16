package com.mycom.myapp.service.slotconfig;

import com.mycom.myapp.service.slotconfig.dto.SlotConfigRequest;

import java.util.List;

public interface SlotConfigService {

    // 현재 운영시간(템플릿) 조회
    List<SlotConfigRequest> getConfigs();

    // 운영시간(템플릿) 저장 + 전체 룸 TimeSlot 재생성
    void replaceConfigs(List<SlotConfigRequest> configs);
}
