package com.exampro.ui.theme;

import java.awt.Color;

/** The single home of color tokens. Dark is default; every pair below meets WCAG AA (>= 4.5:1) for text. */
public enum Theme {
    DARK(c(0x0B1120), c(0x111827), c(0x1B2434), c(0x6366F1), c(0x22D3EE), c(0x22C55E), c(0xF59E0B), c(0xEF4444), c(0xF1F5F9), c(0x94A3B8)),
    LIGHT(c(0xF8FAFC), c(0xFFFFFF), c(0xEEF2F7), c(0x4F46E5), c(0x0E7490), c(0x15803D), c(0xB45309), c(0xB91C1C), c(0x0F172A), c(0x475569));

    public final Color bg, surface, raised, primary, accent, success, warning, danger, text, muted;

    Theme(Color bg, Color surface, Color raised, Color primary, Color accent, Color success, Color warning, Color danger, Color text, Color muted) {
        this.bg = bg; this.surface = surface; this.raised = raised; this.primary = primary; this.accent = accent;
        this.success = success; this.warning = warning; this.danger = danger; this.text = text; this.muted = muted;
    }
    private static Color c(int rgb) { return new Color(rgb); }

    public static double luminance(Color k) {
        double[] v = {k.getRed() / 255.0, k.getGreen() / 255.0, k.getBlue() / 255.0};
        for (int i = 0; i < 3; i++) v[i] = v[i] <= 0.03928 ? v[i] / 12.92 : Math.pow((v[i] + 0.055) / 1.055, 2.4);
        return 0.2126 * v[0] + 0.7152 * v[1] + 0.0722 * v[2];
    }
    public static double contrast(Color a, Color b) {
        double l1 = luminance(a), l2 = luminance(b);
        return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
    }
}
