package com.exampro.util;

import java.util.Optional;
import java.util.regex.Pattern;

public final class Validators {
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private Validators() {}

    public static Optional<String> displayName(String name) {
        if (name == null) return Optional.of("Display name is required");
        String trimmed = name.trim();
        if (trimmed.length() < 2) return Optional.of("Display name must be at least 2 characters");
        if (trimmed.length() > 40) return Optional.of("Display name must be at most 40 characters");
        return Optional.empty();
    }

    public static Optional<String> passwordChange(boolean requireCurrent, String newPass, String confirm) {
            if (requireCurrent) {
                // In real usage, current password would be verified against stored hash
                // Here we just check the new password rules
            }
            if (newPass == null || newPass.isEmpty()) return Optional.of("New password is required");
            if (!newPass.equals(confirm)) return Optional.of("Passwords do not match");
        
            // Different strength requirements:
            // - New users (requireCurrent=false): need STRONG (>=12 chars, 3+ char types)
            // - Existing users changing password (requireCurrent=true): need at least 8 chars and not EMPTY
            if (requireCurrent) {
                if (newPass.length() < 8) return Optional.of("Password must be at least 8 characters");
            } else {
                if (strength(newPass) != Strength.STRONG) return Optional.of("Password is too weak");
            }
            return Optional.empty();
        }

    public static boolean differsFromCurrent(String current, String candidate) {
        return current == null || !current.equals(candidate);
    }

    public enum Strength { EMPTY, WEAK, STRONG }

    public static Strength strength(String s) {
        if (s == null || s.isEmpty()) return Strength.EMPTY;
        boolean hasUpper = UPPER.matcher(s).find();
        boolean hasLower = LOWER.matcher(s).find();
        boolean hasDigit = DIGIT.matcher(s).find();
        boolean hasSpecial = SPECIAL.matcher(s).find();
        int score = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        if (s.length() >= 12 && score >= 3) return Strength.STRONG;
        return Strength.WEAK;
    }
}