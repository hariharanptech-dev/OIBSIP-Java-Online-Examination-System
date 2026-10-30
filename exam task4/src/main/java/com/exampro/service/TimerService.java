package com.exampro.service;

import com.exampro.util.Clock;
import java.util.concurrent.TimeUnit;

/** Absolute-deadline countdown. Remaining time is always derived from the clock, never decremented. */
public final class TimerService {
    public enum Phase { NORMAL, AMBER, RED, FINAL_MINUTE, EXPIRED }
    public static final int[] WARNING_MINUTES = {10, 5, 1};

    private final Clock clock;
    private final long totalNanos;
    private long deadline;
    private boolean started;
    private int lastWarnedIdx = -1;

    public TimerService(Clock clock, long totalSeconds) {
        if (totalSeconds <= 0) throw new IllegalArgumentException("duration must be positive");
        this.clock = clock;
        this.totalNanos = TimeUnit.SECONDS.toNanos(totalSeconds);
    }

    public void start() { deadline = clock.nanoTime() + totalNanos; started = true; lastWarnedIdx = -1; }

    public long remainingNanos() {
        if (!started) return totalNanos;
        return Math.max(0, deadline - clock.nanoTime());
    }
    /** Whole seconds, rounded up so the display shows 0:01 until the last instant. */
    public long remainingSeconds() { return (remainingNanos() + 999_999_999L) / 1_000_000_000L; }
    public long elapsedSeconds() { return TimeUnit.NANOSECONDS.toSeconds(totalNanos - remainingNanos()); }
    public boolean isExpired() { return started && remainingNanos() == 0; }
    /** 1.0 full -> 0.0 empty; drives the shrinking progress bar. */
    public double fractionRemaining() { return (double) remainingNanos() / totalNanos; }

    public Phase phase() {
        if (isExpired()) return Phase.EXPIRED;
        long s = remainingSeconds();
        if (s <= 60) return Phase.FINAL_MINUTE;
        if (s <= 5 * 60) return Phase.RED;
        if (s <= 10 * 60) return Phase.AMBER;
        return Phase.NORMAL;
    }

    /** Returns the minute mark (10/5/1) newly crossed since the last call, or -1. Each fires once. */
    public int pollWarning() {
        long s = remainingSeconds();
        for (int i = 0; i < WARNING_MINUTES.length; i++) {
            if (i > lastWarnedIdx && s <= WARNING_MINUTES[i] * 60L && s > 0 && totalNanos > WARNING_MINUTES[i] * 60L * 1_000_000_000L) {
                lastWarnedIdx = i;
                // skip any smaller marks already passed so a late tick reports only the latest
                while (i + 1 < WARNING_MINUTES.length && s <= WARNING_MINUTES[i + 1] * 60L) lastWarnedIdx = ++i;
                return WARNING_MINUTES[i];
            }
        }
        return -1;
    }

    public static String format(long seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
