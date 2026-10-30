package com.exampro.service;

import com.exampro.model.ShuffledQuestion;
import java.util.*;

/** Pure scoring. selected[i] is the DISPLAY position chosen for question i, or null if unanswered. */
public final class ResultService {
    public record TopicScore(int correct, int total) {}
    public record Result(int correct, int incorrect, int unanswered, int total, double percent, boolean passed,
                         Map<String, TopicScore> byTopic) {
        public String scoreText() { return correct + " out of " + total; }
    }

    private ResultService() {}

    public static Result score(List<ShuffledQuestion> qs, Integer[] selected, double passPercent) {
        if (selected.length != qs.size()) throw new IllegalArgumentException("answers/questions length mismatch");
        int c = 0, w = 0, u = 0;
        Map<String, int[]> topics = new TreeMap<>();
        for (int i = 0; i < qs.size(); i++) {
            ShuffledQuestion q = qs.get(i);
            int[] t = topics.computeIfAbsent(q.question().topic(), k -> new int[2]);
            t[1]++;
            Integer s = selected[i];
            if (s == null || s < 0 || s > 3) u++;
            else if (s == q.correctDisplayIndex()) { c++; t[0]++; }
            else w++;
        }
        double pct = qs.isEmpty() ? 0 : 100.0 * c / qs.size();
        Map<String, TopicScore> bt = new LinkedHashMap<>();
        topics.forEach((k, v) -> bt.put(k, new TopicScore(v[0], v[1])));
        return new Result(c, w, u, qs.size(), pct, pct >= passPercent, bt);
    }
}
