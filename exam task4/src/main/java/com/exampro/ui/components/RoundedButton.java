package com.exampro.ui.components;

import com.exampro.ui.Ui;
import com.exampro.ui.anim.Animator;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/** Gradient button: animated hover glow, spring press-scale, click ripple, focus ring, disabled state. */
public class RoundedButton extends JButton {
    private final boolean ghost;
    private double hover, press, ripple = 1; private Point rippleAt = new Point();

    public RoundedButton(String text, boolean ghost) {
        super(text); this.ghost = ghost;
        setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
        setFont(Ui.font(Font.BOLD, 14)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(150, 46)); getAccessibleContext().setAccessibleName(text);
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { tween(true); }
            public void mouseExited(MouseEvent e) { tween(false); }
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;
                rippleAt = e.getPoint(); Animator.run(450, 0, v -> { ripple = v; repaint(); }, null);
                Animator.run(140, 0, v -> { press = v; repaint(); }, null);
            }
            public void mouseReleased(MouseEvent e) { Animator.run(220, 0, v -> { press = 1 - v; repaint(); }, null); }
        });
        addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { repaint(); } public void focusLost(FocusEvent e) { repaint(); } });
    }
    private void tween(boolean in) { double from = hover; Animator.run(160, 0, v -> { hover = from + ((in ? 1 : 0) - from) * v; repaint(); }, null); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = Ui.g2(g); int w = getWidth(), h = getHeight(); double s = 1 - 0.04 * press;
        g2.translate(w / 2.0, h / 2.0); g2.scale(s, s); g2.translate(-w / 2.0, -h / 2.0);
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 16, 16);
        if (!isEnabled()) g2.setComposite(AlphaComposite.SrcOver.derive(0.4f));
        Color a = ghost ? Ui.mix(Ui.t.raised, Ui.t.text, 0.06 + 0.06 * hover) : Ui.mix(Ui.t.primary, Color.WHITE, 0.14 * hover);
        Color b = ghost ? a : Ui.mix(Ui.t.primary, Ui.t.accent, 0.35 + 0.25 * hover);
        g2.setPaint(new GradientPaint(0, 0, a, w, h, b)); g2.fill(shape);
        if (ghost) { g2.setColor(Ui.alpha(Ui.t.muted, 120)); g2.draw(shape); }
        if (ripple < 1) { g2.clip(shape); g2.setColor(new Color(255, 255, 255, (int) (70 * (1 - ripple))));
            double r = ripple * Math.max(w, h) * 1.2; g2.fill(new Ellipse2D.Double(rippleAt.x - r, rippleAt.y - r, 2 * r, 2 * r)); g2.setClip(null); }
        g2.setFont(getFont()); g2.setColor(ghost ? Ui.t.text : Color.WHITE); Ui.centered(g2, getText(), w / 2.0, h / 2.0);
        if (hasFocus() && isEnabled()) { g2.setColor(Ui.t.accent); g2.setStroke(new BasicStroke(2.5f)); g2.draw(new RoundRectangle2D.Double(-1, -1, w + 2, h + 2, 20, 20)); }
        g2.dispose();
    }
}
