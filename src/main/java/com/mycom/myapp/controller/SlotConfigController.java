package com.mycom.myapp.controller;

import com.mycom.myapp.service.slotconfig.SlotConfigService;
import com.mycom.myapp.service.slotconfig.dto.SlotConfigRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/slots")
public class SlotConfigController {

    private final SlotConfigService slotConfigService;

    @GetMapping("/config")
    public List<SlotConfigRequest> getConfigs() {
        return slotConfigService.getConfigs();
    }

    @PutMapping("/config")
    public void replaceConfigs(@RequestBody List<SlotConfigRequest> configs) {
        slotConfigService.replaceConfigs(configs);
    }
}
