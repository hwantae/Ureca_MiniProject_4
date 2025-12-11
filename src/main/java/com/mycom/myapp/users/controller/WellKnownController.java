package com.mycom.myapp.users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WellKnownController {

    @GetMapping("/.well-known/**")
    public ResponseEntity<Void> ignoreWellKnown() {
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
