package com.exampro.ui.components;

import com.exampro.ui.Ui;
import com.exampro.ui.anim.Animator;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/** A real JRadioButton painted as a large answer card: letter badge, hover lift, selected check mark. */
public final class OptionCard extends JRadioButton {
    private final int idx; private String text = ""; private double hover, pop = 1;

    public OptionCard(int idx) {
        this.idx = idx; setOpaque(false); setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(600, 64)); setMinimumSize(new Dimension(200, 64)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        setAlignmentX(LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { tween(1); } public void mouseExited(MouseEvent e) { tween(0); }
        });
        addActionListener(e -> Animator.run(350, 0, v -> { pop = Animator.back(v); repaint(); }, null));
        addItemListener(e -> repaint());
        addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { repaint(); } public void focusLost(FocusEvent e) { repaint(); } });
    }
    private void tween(double to) { double from = hover; Animator.run(160, 0, v -> { hover = from + (to - from) * v; repaint(); }, null); }
    public void setOptionText(String s) { text = s; getAccessibleContext().setAccessibleName("Option " + (char) ('A' + idx) + ": " + s); repaint(); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = Ui.g2(g); int w = getWidth(), h = getHeight(); boolean sel = isSelected();
        double sc = sel ? 0.985 + 0.015 * pop : 1; g2.translate(w / 2.0, h / 2.0); g2.scale(sc, sc); g2.translate(-w / 2.0, -h / 2.0);
        RoundRectangle2D r = new RoundRectangle2D.Double(1.5, 1.5 - 2 * hover, w - 3, h - 3, 18, 18);
        g2.setColor(Ui.alpha(Color.BLACK, (int) (50 * hover))); g2.fill(new RoundRectangle2D.Double(3, 5 - 2 * hover, w - 6, h - 4, 18, 18));
        g2.setColor(sel ? Ui.mix(Ui.t.raised, Ui.t.primary, 0.28) : Ui.mix(Ui.t.raised, Ui.t.text, 0.05 * hover)); g2.fill(r);
        g2.setStroke(new BasicStroke(sel ? 2.5f : 1.2f)); g2.setColor(sel ? Ui.t.primary : Ui.alpha(Ui.t.muted, 90 + (int) (90 * hover))); g2.draw(r);
        if (hasFocus()) { g2.setColor(Ui.t.accent); g2.setStroke(new BasicStroke(2.5f)); g2.draw(new RoundRectangle2D.Double(-1, -1, w + 2, h + 2, 22, 22)); }
        double cy = h / 2.0 - hover;
        g2.setColor(sel ? Ui.t.primary : Ui.t.bg); g2.fill(new Ellipse2D.Double(16, cy - 17, 34, 34));
        g2.setFont(Ui.font(Font.BOLD, 15)); g2.setColor(sel ? Color.WHITE : Ui.t.muted); Ui.centered(g2, String.valueOf((char) ('A' + idx)), 33, cy);
        g2.setFont(Ui.font(Font.PLAIN, 17)); g2.setColor(Ui.t.text);
        g2.setClip(0, 0, w - 56, h); g2.drawString(text, 64, (float) (cy + g2.getFontMetrics().getAscent() / 2.0 - 2)); g2.setClip(null);
        if (sel) { g2.setColor(Ui.t.success); g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D p = new Path2D.Double(); p.moveTo(w - 40, cy); p.lineTo(w - 33, cy + 7); p.lineTo(w - 21, cy - 7); g2.draw(p); }
        g2.dispose();
    }
}
