package com.exampro;

import static org.junit.jupiter.api.Assertions.*;

import com.exampro.model.*;
import com.exampro.service.*;
import com.exampro.service.TimerService.Phase;
import com.exampro.ui.theme.Theme;
import com.exampro.util.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class CoreTest {
    static final class FakeClock implements Clock {
        long now = 1_000;
        public long nanoTime() { return now; }
        void advanceSec(double s) { now += (long) (s * 1e9); }
    }
    static Question q(long id, String topic, int correct) {
        return new Question(id, topic, "Q" + id, List.of("a", "b", "c", "d"), correct, "why");
    }
    static List<ShuffledQuestion> identity(Question... qs) {
        return Arrays.stream(qs).map(x -> new ShuffledQuestion(x, new int[]{0, 1, 2, 3})).toList();
    }

    // ---- scoring ----
    @Test void scoringCountsCorrectIncorrectUnanswered() {
        var qs = identity(q(1, "Java", 0), q(2, "Java", 1), q(3, "DBMS", 2), q(4, "OS", 3));
        var r = ResultService.score(qs, new Integer[]{0, 3, null, 3}, 50);
        assertEquals(2, r.correct()); assertEquals(1, r.incorrect()); assertEquals(1, r.unanswered());
        assertEquals("2 out of 4", r.scoreText()); assertTrue(r.passed());
        assertEquals(1, r.byTopic().get("Java").correct()); assertEquals(2, r.byTopic().get("Java").total());
    }
    @Test void allUnansweredScoresZeroAndFails() {
        var r = ResultService.score(identity(q(1, "Java", 0), q(2, "OS", 1)), new Integer[]{null, null}, 40);
        assertEquals(0, r.correct()); assertEquals(2, r.unanswered()); assertFalse(r.passed());
    }
    @Test void passBoundaryIsInclusive() {
        var r = ResultService.score(identity(q(1, "A", 0), q(2, "A", 0)), new Integer[]{0, 1}, 50);
        assertTrue(r.passed());
    }
    @Test void lengthMismatchRejected() {
        assertThrows(IllegalArgumentException.class, () -> ResultService.score(identity(q(1, "A", 0)), new Integer[0], 40));
    }

    // ---- shuffle mapping ----
    @Test void shuffleKeepsCorrectAnswerScorable() {
        List<Question> pool = new ArrayList<>();
        for (int i = 0; i < 40; i++) pool.add(q(i, "T" + i % 3, i % 4));
        var exam = ExamShuffler.build(pool, 20, new Random(7));
        assertEquals(20, exam.size());
        assertEquals(20, exam.stream().map(s -> s.question().id()).distinct().count());
        Integer[] picks = new Integer[20];
        for (int i = 0; i < 20; i++) {
            var s = exam.get(i);
            assertEquals(s.question().options().get(s.question().correctIndex()), s.optionText(s.correctDisplayIndex()));
            assertEquals(4, Arrays.stream(s.order()).distinct().count());
            picks[i] = s.correctDisplayIndex();
        }
        assertEquals(20, ResultService.score(exam, picks, 40).correct());
    }
    @Test void shuffleCapsAtPoolSize() {
        assertEquals(2, ExamShuffler.build(List.of(q(1, "A", 0), q(2, "A", 1)), 20, new Random(1)).size());
    }

    // ---- timer ----
    @Test void timerUsesAbsoluteDeadlineAndExpiresExactly() {
        var c = new FakeClock(); var t = new TimerService(c, 1800); t.start();
        assertEquals(1800, t.remainingSeconds()); assertEquals(Phase.NORMAL, t.phase());
        c.advanceSec(1799.5); assertFalse(t.isExpired()); assertEquals(1, t.remainingSeconds());
        c.advanceSec(0.5); assertTrue(t.isExpired()); assertEquals(Phase.EXPIRED, t.phase());
        c.advanceSec(100); assertEquals(0, t.remainingSeconds());
    }
    @Test void timerIgnoresTickCountAndSkippedTicks() {
        var c = new FakeClock(); var t = new TimerService(c, 600); t.start();
        c.advanceSec(250);               // one giant gap, as after a stalled EDT
        assertEquals(350, t.remainingSeconds());
    }
    @Test void timerPhaseBoundaries() {
        var c = new FakeClock(); var t = new TimerService(c, 1800); t.start();
        c.advanceSec(1800 - 601); assertEquals(Phase.NORMAL, t.phase());
        c.advanceSec(1);          assertEquals(Phase.AMBER, t.phase());
        c.advanceSec(300);        assertEquals(Phase.RED, t.phase());
        c.advanceSec(240);        assertEquals(Phase.FINAL_MINUTE, t.phase());
    }
    @Test void warningsFireOncePerMark() {
        var c = new FakeClock(); var t = new TimerService(c, 1800); t.start();
        assertEquals(-1, t.pollWarning());
        c.advanceSec(1200); assertEquals(10, t.pollWarning()); assertEquals(-1, t.pollWarning());
        c.advanceSec(300);  assertEquals(5, t.pollWarning());
        c.advanceSec(240);  assertEquals(1, t.pollWarning()); assertEquals(-1, t.pollWarning());
    }
    @Test void fractionAndFormat() {
        var c = new FakeClock(); var t = new TimerService(c, 100); t.start(); c.advanceSec(25);
        assertEquals(0.75, t.fractionRemaining(), 1e-9); assertEquals("01:05", TimerService.format(65));
        assertThrows(IllegalArgumentException.class, () -> new TimerService(c, 0));
    }

    // ---- auth primitives ----
    @Test void hashingIsSaltedAndVerifies() {
        String a = PasswordHasher.hash("Student@123".toCharArray()), b = PasswordHasher.hash("Student@123".toCharArray());
        assertNotEquals(a, b);
        assertTrue(PasswordHasher.verify("Student@123".toCharArray(), a));
        assertFalse(PasswordHasher.verify("student@123".toCharArray(), a));
        assertFalse(PasswordHasher.verify("x".toCharArray(), "garbage"));
    }
    @Test void lockoutAfterFiveFailuresForThirtySeconds() {
        var c = new FakeClock(); var th = new LoginThrottle(c, 5, 30);
        for (int i = 0; i < 4; i++) th.recordFailure();
        assertFalse(th.isLocked()); th.recordFailure(); assertTrue(th.isLocked());
        assertEquals(30, th.secondsRemaining());
        c.advanceSec(29.9); assertTrue(th.isLocked());
        c.advanceSec(0.2);  assertFalse(th.isLocked());
        th.recordFailure(); assertFalse(th.isLocked());   // counter was reset
    }
    @Test void successResetsFailureCount() {
        var th = new LoginThrottle(new FakeClock(), 5, 30);
        for (int i = 0; i < 4; i++) th.recordFailure();
        th.recordSuccess(); th.recordFailure(); assertFalse(th.isLocked());
    }

    // ---- validators ----
    @Test void displayNameLength() {
        assertTrue(Validators.displayName("A").isPresent()); assertTrue(Validators.displayName(" A ").isPresent());
        assertTrue(Validators.displayName("Al").isEmpty()); assertTrue(Validators.displayName("x".repeat(40)).isEmpty());
        assertTrue(Validators.displayName("x".repeat(41)).isPresent()); assertTrue(Validators.displayName(null).isPresent());
    }
    @Test void passwordChangeRules() {
        assertTrue(Validators.passwordChange(false, "Newpass@99", "Newpass@99").isPresent());
        assertTrue(Validators.passwordChange(true, "short", "short").isPresent());
        assertTrue(Validators.passwordChange(true, "Newpass@99", "Newpass@98").isPresent());
        assertTrue(Validators.passwordChange(true, "Newpass@99", "Newpass@99").isEmpty());
        assertFalse(Validators.differsFromCurrent("Same@1234", "Same@1234"));
        assertTrue(Validators.differsFromCurrent("Same@1234", "Diff@1234"));
    }
    @Test void strengthLevels() {
        assertEquals(Validators.Strength.EMPTY, Validators.strength(""));
        assertEquals(Validators.Strength.WEAK, Validators.strength("abc"));
        assertEquals(Validators.Strength.STRONG, Validators.strength("Corr3ct-Horse-Battery"));
    }

    // ---- design tokens ----
    @Test void themesMeetWcagAaForText() {
        for (Theme th : Theme.values()) {
            assertTrue(Theme.contrast(th.text, th.bg) >= 4.5, th + " text/bg");
            assertTrue(Theme.contrast(th.text, th.surface) >= 4.5, th + " text/surface");
            assertTrue(Theme.contrast(th.muted, th.bg) >= 4.5, th + " muted/bg");
            assertTrue(Theme.contrast(th.muted, th.surface) >= 4.5, th + " muted/surface");
            assertTrue(Theme.contrast(th.muted, th.raised) >= 4.5, th + " muted/raised");
            for (var k : List.of(th.success, th.warning, th.danger, th.accent))
                assertTrue(Theme.contrast(k, th.surface) >= 3.0, th + " status color on surface (UI/large-text 3:1)");
        }
    }
}
