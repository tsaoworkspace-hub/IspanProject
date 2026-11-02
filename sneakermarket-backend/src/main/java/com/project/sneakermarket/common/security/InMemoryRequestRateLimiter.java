package com.project.sneakermarket.common.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InMemoryRequestRateLimiter {

    private static final int CLEANUP_THRESHOLD = 10_000;

    private final ConcurrentHashMap<String, WindowCounter> counters =
            new ConcurrentHashMap<>();

    public void check(
            String scope,
            String clientId,
            int maxRequests,
            Duration window
    ) {
        long windowMillis = window.toMillis();
        long currentWindow = System.currentTimeMillis() / windowMillis;
        String safeClientId = clientId == null || clientId.isBlank()
                ? "unknown"
                : clientId;
        String key = scope + ':' + safeClientId;

        WindowCounter counter = counters.compute(
                key,
                (ignored, existing) -> {
                    if (existing == null || existing.windowId() != currentWindow) {
                        return new WindowCounter(
                                currentWindow,
                                new AtomicInteger()
                        );
                    }
                    return existing;
                }
        );

        if (counter.count().incrementAndGet() > maxRequests) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "請求過於頻繁，請稍後再試"
            );
        }

        if (counters.size() > CLEANUP_THRESHOLD) {
            counters.entrySet().removeIf(
                    entry -> entry.getValue().windowId() < currentWindow - 1
            );
        }
    }

    private record WindowCounter(
            long windowId,
            AtomicInteger count
    ) {
    }
}
