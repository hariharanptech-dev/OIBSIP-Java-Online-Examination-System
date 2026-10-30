package com.exampro.ui;

import com.exampro.ui.theme.Theme;
import java.awt.*;
import java.util.*;
import javax.swing.JLabel;

public final class Ui {
    public static Theme t = Theme.DARK;
    private static final Set<String> FAM = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
    private Ui() {}

    public static Font font(int style, float size) {
        String n = FAM.contains("Inter") ? "Inter" : FAM.contains("Segoe UI") ? "Segoe UI" : Font.SANS_SERIF;
        return new Font(n, style, 12).deriveFont(size);
    }
    public static Graphics2D g2(Graphics g) {
        Graphics2D x = (Graphics2D) g.create();
        x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        x.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return x;
    }
    public static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a))); }
    public static Color mix(Color a, Color b, double f) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * f), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * f),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * f));
    }
    public static JLabel label(String s, float size, boolean bold, Color c) {
        JLabel l = new JLabel(s); l.setFont(font(bold ? Font.BOLD : Font.PLAIN, size)); l.setForeground(c); return l;
    }
    public static void centered(Graphics2D g, String s, double cx, double cy) {
        FontMetrics m = g.getFontMetrics();
        g.drawString(s, (float) (cx - m.stringWidth(s) / 2.0), (float) (cy + (m.getAscent() - m.getDescent()) / 2.0));
    }
}
