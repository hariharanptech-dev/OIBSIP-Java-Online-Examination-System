package com.exampro.util;

public final class LoginThrottle {
    private final Clock clock;
    private final int maxFailures;
    private final int lockoutSeconds;
    private int failures;
    private long lockUntil;

    public LoginThrottle(Clock clock, int maxFailures, int lockoutSeconds) {
        this.clock = clock;
        this.maxFailures = maxFailures;
        this.lockoutSeconds = lockoutSeconds;
    }

    public void recordFailure() {
        if (isLocked()) return;
        failures++;
        if (failures >= maxFailures) {
            lockUntil = clock.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(lockoutSeconds);
        }
    }

    public void recordSuccess() {
        failures = 0;
        lockUntil = 0;
    }

    public boolean isLocked() {
        if (lockUntil == 0) return false;
        if (clock.nanoTime() >= lockUntil) {
            lockUntil = 0;
            failures = 0;
            return false;
        }
        return true;
    }

    public int secondsRemaining() {
        if (!isLocked()) return 0;
            return (int) java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(lockUntil - clock.nanoTime());
    }
}