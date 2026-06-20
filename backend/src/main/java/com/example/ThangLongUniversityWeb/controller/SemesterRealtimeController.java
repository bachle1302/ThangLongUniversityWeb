package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.service.SemesterRealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/realtime/semesters")
@RequiredArgsConstructor
public class SemesterRealtimeController {

    private final SemesterRealtimeService semesterRealtimeService;

    @GetMapping(value = "/{semesterId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long semesterId) {
        return semesterRealtimeService.subscribe(semesterId);
    }
}
