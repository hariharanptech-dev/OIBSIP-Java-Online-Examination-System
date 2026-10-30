package com.exampro.model;

import java.util.List;

public record Question(
    long id,
    String topic,
    String prompt,
    List<String> options,
    int correctIndex,
    String explanation
) {}