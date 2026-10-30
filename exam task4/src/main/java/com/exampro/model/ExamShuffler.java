package com.exampro.model;

import java.util.*;

public final class ExamShuffler {
    private ExamShuffler() {}

    public static List<ShuffledQuestion> build(List<Question> pool, int count, Random rng) {
        if (pool.isEmpty() || count <= 0) return List.of();
        int n = Math.min(count, pool.size());
        List<Question> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, rng);
        List<ShuffledQuestion> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Question q = shuffled.get(i);
            int[] order = new int[4];
            for (int j = 0; j < 4; j++) order[j] = j;
            // Fisher-Yates shuffle for options
            for (int j = 3; j > 0; j--) {
                int k = rng.nextInt(j + 1);
                int tmp = order[j];
                order[j] = order[k];
                order[k] = tmp;
            }
            result.add(new ShuffledQuestion(q, order));
        }
        return result;
    }
}