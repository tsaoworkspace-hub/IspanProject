package com.project.sneakermarket.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryRequestRateLimiterTests {

    @Test
    void rejectsRequestsThatExceedTheWindowLimit() {
        InMemoryRequestRateLimiter limiter =
                new InMemoryRequestRateLimiter();

        assertDoesNotThrow(() -> limiter.check(
                "login",
                "127.0.0.1",
                1,
                Duration.ofMinutes(1)
        ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> limiter.check(
                        "login",
                        "127.0.0.1",
                        1,
                        Duration.ofMinutes(1)
                )
        );

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                exception.getStatusCode()
        );
    }

    @Test
    void keepsDifferentScopesIndependent() {
        InMemoryRequestRateLimiter limiter =
                new InMemoryRequestRateLimiter();

        assertDoesNotThrow(() -> limiter.check(
                "login",
                "127.0.0.1",
                1,
                Duration.ofMinutes(1)
        ));
        assertDoesNotThrow(() -> limiter.check(
                "chatbot",
                "127.0.0.1",
                1,
                Duration.ofMinutes(1)
        ));
    }
}
