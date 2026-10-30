package com.exampro.model;

import java.util.List;

public record ShuffledQuestion(
    Question question,
    int[] order
) {
    public String optionText(int displayIndex) {
        return question.options().get(order[displayIndex]);
    }
    public int correctDisplayIndex() {
        for (int i = 0; i < order.length; i++) {
            if (order[i] == question.correctIndex()) return i;
        }
        throw new IllegalStateException("Correct index not found in shuffle order");
    }
}