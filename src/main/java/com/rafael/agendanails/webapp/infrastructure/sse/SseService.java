package com.rafael.agendanails.webapp.infrastructure.sse;

import com.rafael.agendanails.webapp.infrastructure.dto.sse.SsePayloadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class SseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void addEmitter(Long userId, SseEmitter emitter) {

        emitters.put(userId, emitter);

        Runnable removeCallback = () -> {
            log.info("Removing user: {} emitter", userId);

            emitters.remove(userId);
        };
        emitter.onCompletion(removeCallback);
        emitter.onTimeout(removeCallback);
        emitter.onError(e -> removeCallback.run());
    }

    public void sendEventToUser(Long userId, SsePayloadDTO payload) {

        var emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                emitter.send(payload);
            } catch (IOException e) {
                log.info("Failed to send payload to user: {} emitter", userId, e);
                throw new RuntimeException(e);
            }
        }
    }

    public void removeEmitter(Long userId) {
        SseEmitter emitter = emitters.remove(userId);

        if (emitter != null) {
            emitter.complete();
        }
    }
}