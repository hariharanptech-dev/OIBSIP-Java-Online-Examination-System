package com.exampro.ui.anim;

import java.util.*;
import java.util.function.DoubleConsumer;
import javax.swing.Timer;

/** One shared 60 fps timer for all tweens. It stops itself when nothing is animating. */
public final class Animator {
    public static boolean reduceMotion;
    private record Job(long start, int ms, DoubleConsumer f, Runnable done) {}
    private static final List<Job> JOBS = new ArrayList<>();
    private static final Timer TIMER = new Timer(16, e -> tick());
    private Animator() {}

    public static double easeOut(double t) { return 1 - Math.pow(1 - t, 3); }
    /** Spring-like overshoot, used for press/pop effects. */
    public static double back(double t) { double c = 1.70158; double u = t - 1; return 1 + (c + 1) * u * u * u + c * u * u; }

    public static void run(int ms, int delay, DoubleConsumer f, Runnable done) {
        if (reduceMotion) { f.accept(1); if (done != null) done.run(); return; }
        JOBS.add(new Job(System.nanoTime() / 1_000_000 + delay, ms, f, done));
        TIMER.start();
    }
    private static void tick() {
        long now = System.nanoTime() / 1_000_000;
        for (Job j : new ArrayList<>(JOBS)) {
            if (now < j.start) continue;
            double t = Math.min(1, (now - j.start) / (double) j.ms);
            j.f.accept(easeOut(t));
            if (t >= 1) { JOBS.remove(j); if (j.done != null) j.done.run(); }
        }
        if (JOBS.isEmpty()) TIMER.stop();
    }
}
