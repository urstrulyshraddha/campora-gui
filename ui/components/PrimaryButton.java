package ui.components;

import ui.theme.UITheme;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PrimaryButton extends JButton {
    private Color normalColor;
    private Color hoverColor;
    private Color textColor;
    private boolean isHovered = false;
    private int cornerRadius;

    public PrimaryButton(String text) {
        this(text, UITheme.PURPLE_PRIMARY, UITheme.PURPLE_HOVER, Color.WHITE, 12);
    }

    public PrimaryButton(String text, Color normal, Color hover, Color textCol, int radius) {
        super(text);
        this.normalColor = normal;
        this.hoverColor = hover;
        this.textColor = textCol;
        this.cornerRadius = radius;

        setFont(UITheme.FONT_BODY_BOLD);
        setForeground(textColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(isHovered ? hoverColor : normalColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        super.paintComponent(g);
        g2.dispose();
    }
}
