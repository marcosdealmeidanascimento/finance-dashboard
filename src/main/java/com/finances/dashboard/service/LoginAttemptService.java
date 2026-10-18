package com.finances.dashboard.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.finances.dashboard.exception.TooManyRequestsException;

/**
 * Brute-force protection for login: after MAX_FAILURES wrong passwords for the same email + client IP
 * within WINDOW, further attempts get a 429 until the window ends. A successful login clears the count.
 *
 * In-memory on purpose (single instance, nothing to deploy). Behind Docker's port forwarding every client
 * shares the same remote IP, so in practice the limit is per email.
 */
@Service
public class LoginAttemptService {
    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private record Attempts(int failures, Instant windowStart) {
    }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public static String key(String normalizedEmail, String clientIp) {
        return normalizedEmail + "|" + clientIp;
    }

    public void checkAllowed(String key) {
        Attempts current = attempts.get(key);
        if (current == null || isExpired(current) || current.failures() < MAX_FAILURES) {
            return;
        }
        long minutesLeft = Math.max(1,
                Duration.between(clock.instant(), current.windowStart().plus(WINDOW)).toMinutes() + 1);
        throw new TooManyRequestsException(
                "Muitas tentativas de login. Tente novamente em " + minutesLeft + " minuto(s).");
    }

    public void recordFailure(String key) {
        attempts.compute(key, (k, current) -> current == null || isExpired(current)
                ? new Attempts(1, clock.instant())
                : new Attempts(current.failures() + 1, current.windowStart()));
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    // Keeps the map from growing with stale entries.
    @Scheduled(fixedRate = 10 * 60 * 1000)
    void evictExpired() {
        attempts.values().removeIf(this::isExpired);
    }

    private boolean isExpired(Attempts value) {
        return clock.instant().isAfter(value.windowStart().plus(WINDOW));
    }
}
