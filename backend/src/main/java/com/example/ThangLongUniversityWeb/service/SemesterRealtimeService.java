package com.example.ThangLongUniversityWeb.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SemesterRealtimeService {

    private static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long semesterId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        emitters.computeIfAbsent(semesterId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> remove(semesterId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("semesterId", semesterId, "timestamp", Instant.now().toString())));
        } catch (IOException exception) {
            cleanup.run();
        }
        return emitter;
    }

    public void publishAfterCommit(Long semesterId, String type) {
        if (semesterId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(semesterId, type);
                }
            });
        } else {
            publish(semesterId, type);
        }
    }

    private void publish(Long semesterId, String type) {
        var semesterEmitters = emitters.get(semesterId);
        if (semesterEmitters == null) {
            return;
        }
        Map<String, Object> payload = Map.of(
                "semesterId", semesterId,
                "type", type,
                "timestamp", Instant.now().toString());
        for (SseEmitter emitter : semesterEmitters) {
            try {
                emitter.send(SseEmitter.event().name("semester-update").data(payload));
            } catch (IOException | IllegalStateException exception) {
                remove(semesterId, emitter);
            }
        }
    }

    private void remove(Long semesterId, SseEmitter emitter) {
        var semesterEmitters = emitters.get(semesterId);
        if (semesterEmitters == null) {
            return;
        }
        semesterEmitters.remove(emitter);
        if (semesterEmitters.isEmpty()) {
            emitters.remove(semesterId, semesterEmitters);
        }
    }
}
