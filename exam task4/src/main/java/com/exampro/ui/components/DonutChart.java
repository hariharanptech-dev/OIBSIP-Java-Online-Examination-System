package com.exampro.ui.components;

import com.exampro.ui.Ui;
import com.exampro.ui.anim.Animator;
import java.awt.*;
import java.awt.geom.*;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;

public final class DonutChart extends JComponent {
    private double prog; private int c, t;
    public void setScore(int correct, int total) {
        c = correct; t = Math.max(1, total);
        Animator.run(1200, 150, v -> { prog = v; repaint(); }, null);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = Ui.g2(g); int d = Math.min(getWidth(), getHeight()) - 30; double x = (getWidth() - d) / 2.0, y = (getHeight() - d) / 2.0;
        g2.setStroke(new BasicStroke(24f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Ui.t.raised); g2.draw(new Ellipse2D.Double(x, y, d, d));
        g2.setPaint(new GradientPaint((float) x, (float) y, Ui.t.success, (float) (x + d), (float) (y + d), Ui.t.accent));
        double extent = -360.0 * prog * c / t; if (c > 0) g2.draw(new Arc2D.Double(x, y, d, d, 90, extent, Arc2D.OPEN));
        g2.setColor(Ui.t.text); g2.setFont(Ui.font(Font.BOLD, d / 3.2f)); Ui.centered(g2, String.valueOf(Math.round(prog * c)), getWidth() / 2.0, getHeight() / 2.0 - 8);
        g2.setColor(Ui.t.muted); g2.setFont(Ui.font(Font.PLAIN, 16)); Ui.centered(g2, "out of " + t, getWidth() / 2.0, getHeight() / 2.0 + d / 5.0);
        g2.dispose();
    }
}
