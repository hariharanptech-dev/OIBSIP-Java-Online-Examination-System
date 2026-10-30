package com.exampro.ui.components;

import com.exampro.ui.Ui;
import com.exampro.ui.anim.Animator;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** In-app glass pane: dim-backdrop confirm dialogs, toasts, big banners, 3-2-1 countdown, fade-through transitions. No JOptionPane. */
public final class Overlay extends JPanel {
    private float dim, toastA, flash, bannerScale = 1; private String toast, banner; private JPanel card; private RoundedButton first;

    public Overlay() { super(new GridBagLayout()); setOpaque(false); setVisible(true); }
    public boolean isOpen() { return card != null; }
    @Override public boolean contains(int x, int y) { return (card != null || banner != null || flash > 0) && super.contains(x, y); }

    private void fade(float from, float to, Runnable done) { Animator.run(220, 0, v -> { dim = from + (to - from) * (float) v; repaint(); }, done); }

    public void confirm(String title, String msg, String yes, String no, Runnable onYes) {
        removeAll(); banner = null;
        card = new JPanel() { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = Ui.g2(g);
            g2.setColor(Ui.t.surface); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
            g2.setColor(Ui.alpha(Ui.t.muted, 110)); g2.draw(new RoundRectangle2D.Double(.5, .5, getWidth() - 1, getHeight() - 1, 24, 24)); g2.dispose(); } };
        card.setOpaque(false); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.getAccessibleContext().setAccessibleName(title); card.setFocusCycleRoot(true);
        card.add(Ui.label(title, 22, true, Ui.t.text)); card.add(Box.createVerticalStrut(12));
        for (String line : msg.split("\n")) card.add(Ui.label(line, 15, false, Ui.t.muted));
        card.add(Box.createVerticalStrut(24));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)); row.setOpaque(false);
        first = new RoundedButton(yes, false); RoundedButton b = new RoundedButton(no, true);
        first.addActionListener(e -> dismiss(onYes)); b.addActionListener(e -> dismiss(null));
        row.add(first); row.add(b); card.add(row); row.setAlignmentX(LEFT_ALIGNMENT);
        add(card); revalidate(); fade(dim, 1, () -> first.requestFocusInWindow()); first.requestFocusInWindow();
    }
    public void dismiss() { dismiss(null); }
    public void dismiss(Runnable after) { fade(1, 0, () -> { removeAll(); card = null; revalidate(); repaint(); if (after != null) after.run(); }); }
    public void hideAll() { removeAll(); card = null; banner = null; dim = 0; flash = 0; revalidate(); repaint(); }

    public void banner(String s) { removeAll(); card = null; banner = s; bannerScale = 1.4f; fade(dim, 1, null); Animator.run(400, 0, v -> { bannerScale = 1.4f - 0.4f * (float) v; repaint(); }, null); }
    public void countdown(int from, Runnable done) {
        banner(String.valueOf(from));
        javax.swing.Timer t = new javax.swing.Timer(800, null); int[] n = {from};
        t.addActionListener(e -> { if (--n[0] > 0) banner(String.valueOf(n[0])); else { t.stop(); banner = null; fade(1, 0, null); done.run(); } });
        t.start();
    }
    public void toast(String s) {
        toast = s; Animator.run(250, 0, v -> { toastA = (float) v; repaint(); }, null);
        javax.swing.Timer t = new javax.swing.Timer(2800, e -> Animator.run(350, 0, v -> { toastA = 1 - (float) v; repaint(); }, null)); t.setRepeats(false); t.start();
    }
    /** Fade to the page background, run mid, then fade back. */
    public void transition(Runnable mid) { Animator.run(260, 0, v -> { flash = (float) v; repaint(); }, () -> { mid.run(); Animator.run(320, 0, v -> { flash = 1 - (float) v; repaint(); }, () -> flash = 0); }); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = Ui.g2(g);
        if (dim > 0) { g2.setColor(new Color(2, 6, 23, (int) (190 * dim))); g2.fillRect(0, 0, getWidth(), getHeight()); }
        if (banner != null) { g2.setColor(Ui.t.text); g2.setFont(Ui.font(Font.BOLD, 120 * bannerScale)); Ui.centered(g2, banner.length() > 2 ? "" : banner, getWidth() / 2.0, getHeight() / 2.0);
            if (banner.length() > 2) { g2.setFont(Ui.font(Font.BOLD, 36)); Ui.centered(g2, banner, getWidth() / 2.0, getHeight() / 2.0); } }
        if (toast != null && toastA > 0) { g2.setFont(Ui.font(Font.BOLD, 15)); int w = g2.getFontMetrics().stringWidth(toast) + 64, h = 46;
            double x = (getWidth() - w) / 2.0, y = 18 + 14 * toastA - 14; g2.setComposite(AlphaComposite.SrcOver.derive(toastA));
            g2.setColor(Ui.t.warning); g2.fill(new RoundRectangle2D.Double(x, y, w, h, 24, 24)); g2.setColor(Color.BLACK); Ui.centered(g2, "!  " + toast, getWidth() / 2.0, y + h / 2.0); }
        if (flash > 0) { g2.setColor(Ui.alpha(Ui.t.bg, (int) (255 * flash))); g2.fillRect(0, 0, getWidth(), getHeight()); }
        g2.dispose();
    }
}
